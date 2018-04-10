/*
 * SonarQube Go Plugin
 * Copyright (C) 2018-2018 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.uast.generator.java;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sonar.sslr.api.typed.ActionParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.lang.model.SourceVersion;
import org.sonar.java.ast.parser.JavaParser;
import org.sonar.java.model.JavaTree;
import org.sonar.plugins.java.api.tree.Arguments;
import org.sonar.plugins.java.api.tree.AssignmentExpressionTree;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.BinaryExpressionTree;
import org.sonar.plugins.java.api.tree.BreakStatementTree;
import org.sonar.plugins.java.api.tree.CaseLabelTree;
import org.sonar.plugins.java.api.tree.CompilationUnitTree;
import org.sonar.plugins.java.api.tree.ContinueStatementTree;
import org.sonar.plugins.java.api.tree.ExpressionTree;
import org.sonar.plugins.java.api.tree.IdentifierTree;
import org.sonar.plugins.java.api.tree.IfStatementTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.ParenthesizedTree;
import org.sonar.plugins.java.api.tree.PrimitiveTypeTree;
import org.sonar.plugins.java.api.tree.StatementTree;
import org.sonar.plugins.java.api.tree.SyntaxToken;
import org.sonar.plugins.java.api.tree.SyntaxTrivia;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.UnaryExpressionTree;
import org.sonar.plugins.java.api.tree.VariableTree;

public class Generator {

  public static void main(String[] args) throws IOException {
    Path path = Paths.get(args[0]);
    if (path.toFile().isDirectory()) {
      try (Stream<Path> files = Files.walk(path)) {
        files.filter(p -> p.toString().endsWith(".java"))
          .forEach(Generator::createUastFile);
      }
    } else {
      Generator.createUastFile(path);
    }
  }

  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final ActionParser<Tree> PARSER = JavaParser.createParser();
  private final CompilationUnitTree cut;
  private final UastNode uast;
  private Map<Tree, UastNode> treeUastNodeMap = new HashMap<>();
  private Set<Tree> seenTrivia = new HashSet<>();

  public Generator(String source) {
    cut = (CompilationUnitTree) PARSER.parse(source);
    uast = visit(cut).findFirst().get();
    cut.accept(new PostprocessVisitor());
  }

  private static String readFile(Path path) {
    try {
      return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new GeneratorException(e);
    }
  }

  private static void createUastFile(Path p) {
    try {
      String source = readFile(p);
      String uast = new Generator(source).json();
      Files.write(Paths.get(p.toString() + ".uast.json"), uast.getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
      throw new GeneratorException(e);
    }
  }

  public UastNode uast() {
    return uast;
  }

  public String json() {
    return GSON.toJson(uast);
  }

  private Stream<UastNode> visit(Tree tree) {
    UastNode uastNode = null;
    if (tree.is(Tree.Kind.TOKEN)) {
      uastNode = newUastNode(tree, Collections.emptyList());
      if (uastNode.token != null && SourceVersion.isKeyword(uastNode.token.value)) {
        uastNode.kinds.add(UastNode.Kind.KEYWORD);
      }
      treeUastNodeMap.put(tree, uastNode);
      List<UastNode> trivia = ((SyntaxToken) tree).trivias().stream()
        // SonarJava AST duplicates some nodes (e.g. Variable)
        .filter(seenTrivia::add)
        .map(syntaxTrivia -> newUastNode(syntaxTrivia, Collections.emptyList()))
        .collect(Collectors.toList());
      return Stream.concat(trivia.stream(), Stream.of(uastNode));
    } else if (!tree.is(Tree.Kind.INFERED_TYPE)) {
      List<Tree> children = ((JavaTree) tree).getChildren();
      if (!children.isEmpty()) {
        uastNode = newUastNode(tree, children.stream().flatMap(this::visit).filter(Objects::nonNull).collect(Collectors.toList()));
      }
    }
    if (uastNode != null) {
      treeUastNodeMap.put(tree, uastNode);
    }
    return uastNode == null ? Stream.empty() : Stream.of(uastNode);
  }

  private static UastNode newUastNode(Tree tree, List<UastNode> children) {
    return new UastNode(
            uastKind(tree),
            tree.kind().name(),
            tree.is(Tree.Kind.TOKEN, Tree.Kind.TRIVIA) ? newToken(tree) : null,
            children
    );
  }

  private static UastNode.Token newToken(Tree javaToken) {
    int line;
    int column;
    String text;
    if (javaToken instanceof SyntaxToken) {
      line = ((SyntaxToken) javaToken).line();
      column = ((SyntaxToken) javaToken).column();
      text = ((SyntaxToken) javaToken).text();
    } else {
      line = ((SyntaxTrivia) javaToken).startLine();
      column = ((SyntaxTrivia) javaToken).column();
      text = ((SyntaxTrivia) javaToken).comment();
    }
    return new UastNode.Token(
            line,
            // as per UAST specification column starts at 1
            column + 1,
            text
    );
  }

  private static Set<UastNode.Kind> uastKind(Tree tree) {
    Set<UastNode.Kind> result = EnumSet.noneOf(UastNode.Kind.class);
    switch (tree.kind()) {
      case COMPILATION_UNIT:
        result.add(UastNode.Kind.COMPILATION_UNIT);
        break;
      case METHOD:
      case CONSTRUCTOR:
        result.add(UastNode.Kind.FUNCTION);
        break;
      case LAMBDA_EXPRESSION:
        result.add(UastNode.Kind.FUNCTION_LITERAL);
        break;
      case CLASS:
      case ENUM:
      case INTERFACE:
        result.add(UastNode.Kind.CLASS);
        result.add(UastNode.Kind.TYPE);
        break;
      case ASSIGNMENT:
        result.add(UastNode.Kind.ASSIGNMENT);
        break;
      case AND_ASSIGNMENT:
      case DIVIDE_ASSIGNMENT:
      case LEFT_SHIFT_ASSIGNMENT:
      case MINUS_ASSIGNMENT:
      case MULTIPLY_ASSIGNMENT:
      case OR_ASSIGNMENT:
      case PLUS_ASSIGNMENT:
      case REMAINDER_ASSIGNMENT:
      case RIGHT_SHIFT_ASSIGNMENT:
      case UNSIGNED_RIGHT_SHIFT_ASSIGNMENT:
      case XOR_ASSIGNMENT:
        addTokenKinds(((AssignmentExpressionTree) tree).operatorToken(), result);
        break;
      case BLOCK:
        result.add(UastNode.Kind.BLOCK);
        break;
      case IDENTIFIER:
        result.add(UastNode.Kind.IDENTIFIER);
        if (tree.parent() instanceof BreakStatementTree || tree.parent() instanceof ContinueStatementTree) {
          result.add(UastNode.Kind.BRANCH_LABEL);
        }
        break;
      case STRING_LITERAL:
        result.add(UastNode.Kind.LITERAL);
        result.add(UastNode.Kind.STRING_LITERAL);
        break;
      case FOR_EACH_STATEMENT:
      case FOR_STATEMENT:
      case WHILE_STATEMENT:
      case DO_STATEMENT:
        result.add(UastNode.Kind.LOOP);
        break;
      case IF_STATEMENT:
        result.add(UastNode.Kind.IF);
        break;
      case SWITCH_STATEMENT:
        result.add(UastNode.Kind.SWITCH);
        break;
      case CASE_LABEL:
        result.add(UastNode.Kind.CASE);
        if ("default".equals(((CaseLabelTree) tree).caseOrDefaultKeyword().text())) {
          result.add(UastNode.Kind.DEFAULT_CASE);
        }
        break;
      case TRIVIA:
        result.add(UastNode.Kind.COMMENT);
        break;
      case BOOLEAN_LITERAL:
        result.add(UastNode.Kind.LITERAL);
        result.add(UastNode.Kind.BOOLEAN_LITERAL);
        break;
      case TOKEN:
        addTokenKinds((SyntaxToken) tree, result);
        break;
      case PARENTHESIZED_EXPRESSION:
        result.add(UastNode.Kind.PARENTHESIZED_EXPRESSION);
        break;
      case BREAK_STATEMENT:
        result.add(UastNode.Kind.BREAK);
        break;
      case RETURN_STATEMENT:
        result.add(UastNode.Kind.RETURN);
        break;
      case CONTINUE_STATEMENT:
        result.add(UastNode.Kind.CONTINUE);
        break;
      case THROW_STATEMENT:
        result.add(UastNode.Kind.THROW);
        break;
      case UNARY_MINUS:
        result.add(UastNode.Kind.UNARY_MINUS);
        break;
      case UNARY_PLUS:
        result.add(UastNode.Kind.UNARY_PLUS);
        break;
      case LOGICAL_COMPLEMENT:
        result.add(UastNode.Kind.LOGICAL_COMPLEMENT);
        break;
      case BITWISE_COMPLEMENT:
        result.add(UastNode.Kind.BITWISE_COMPLEMENT);
        break;
      case PREFIX_DECREMENT:
        result.add(UastNode.Kind.PREFIX_DECREMENT);
        break;
      case PREFIX_INCREMENT:
        result.add(UastNode.Kind.PREFIX_INCREMENT);
        break;
      case POSTFIX_DECREMENT:
        result.add(UastNode.Kind.POSTFIX_DECREMENT);
        break;
      case POSTFIX_INCREMENT:
        result.add(UastNode.Kind.POSTFIX_INCREMENT);
        break;
      case ANNOTATION:
        result.add(UastNode.Kind.ANNOTATION);
        break;
      case ANNOTATION_TYPE:
        result.add(UastNode.Kind.CLASS);
        result.add(UastNode.Kind.ANNOTATION_TYPE);
        result.add(UastNode.Kind.TYPE);
        break;
      case CATCH:
        result.add(UastNode.Kind.CATCH);
        break;
      default:
        break;
    }
    if (tree instanceof StatementTree) {
      result.add(UastNode.Kind.STATEMENT);
    }
    if (isExpression(tree)) {
      result.add(UastNode.Kind.EXPRESSION);
    }
    if (tree instanceof BinaryExpressionTree) {
      result.add(UastNode.Kind.BINARY_EXPRESSION);
    }
    if (tree instanceof UnaryExpressionTree) {
      result.add(UastNode.Kind.UNARY_EXPRESSION);
    }
    return result;
  }

  private static void addTokenKinds(SyntaxToken tree, Set<UastNode.Kind> result) {
    String tokenText = tree.text();
    for (UastNode.Kind kind : UastNode.Kind.values()) {
      if (kind.isKindForToken(tokenText)) {
        result.add(kind);
        result.addAll(kind.impliedKinds);
      }
    }
  }

  private static boolean isExpression(Tree tree) {
    if (!(tree instanceof ExpressionTree) || (tree instanceof PrimitiveTypeTree)) {
      return false;
    }
    return (tree.parent() instanceof Arguments) ||
      (tree.parent() instanceof ParenthesizedTree) ||
      !(tree instanceof IdentifierTree);
  }

  class PostprocessVisitor extends BaseTreeVisitor {

    @Override
    public void visitAssignmentExpression(AssignmentExpressionTree tree) {
      ExpressionTree variable = tree.variable();
      ExpressionTree expression = tree.expression();
      addKind(variable, UastNode.Kind.ASSIGNMENT_TARGET);
      addKind(tree.operatorToken(), UastNode.Kind.ASSIGNMENT_OPERATOR);
      addKind(expression, UastNode.Kind.ASSIGNMENT_VALUE);
      super.visitAssignmentExpression(tree);
    }

    @Override
    public void visitVariable(VariableTree tree) {
      super.visitVariable(tree);
      if (tree.initializer() != null) {
        addKind(tree, UastNode.Kind.ASSIGNMENT);
        addKind(tree.simpleName(), UastNode.Kind.ASSIGNMENT_TARGET);
        addKind(tree.equalToken(), UastNode.Kind.ASSIGNMENT_OPERATOR);
        addKind(tree.initializer(), UastNode.Kind.ASSIGNMENT_VALUE);
      }
    }

    @Override
    public void visitMethod(MethodTree tree) {
      addKind(tree.simpleName(), UastNode.Kind.FUNCTION_NAME);
      tree.parameters().forEach(p -> treeUastNodeMap.get(p).kinds.add(UastNode.Kind.PARAMETER));
      addKind(tree.returnType(), UastNode.Kind.RESULT_LIST);
      super.visitMethod(tree);
    }

    @Override
    public void visitIfStatement(IfStatementTree tree) {
      addKind(tree.condition(), UastNode.Kind.CONDITION);
      addKind(tree.thenStatement(), UastNode.Kind.THEN);
      StatementTree elseStatement = tree.elseStatement();
      if (elseStatement != null) {
        addKind(elseStatement, UastNode.Kind.ELSE);
      }
      super.visitIfStatement(tree);
    }

    private void addKind(@Nullable Tree tree, UastNode.Kind kind) {
      if (tree == null) {
        return;
      }
      treeUastNodeMap.get(tree).kinds.add(kind);
    }
  }

  static class GeneratorException extends RuntimeException {
    public GeneratorException(Throwable cause) {
      super(cause);
    }
  }
}
