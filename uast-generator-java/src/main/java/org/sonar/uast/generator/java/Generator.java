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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.lang.model.SourceVersion;
import org.sonar.java.ast.parser.JavaParser;
import org.sonar.java.model.InternalSyntaxToken;
import org.sonar.java.model.JavaTree;
import org.sonar.plugins.java.api.tree.Arguments;
import org.sonar.plugins.java.api.tree.ArrayAccessExpressionTree;
import org.sonar.plugins.java.api.tree.AssignmentExpressionTree;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.BinaryExpressionTree;
import org.sonar.plugins.java.api.tree.BreakStatementTree;
import org.sonar.plugins.java.api.tree.CaseLabelTree;
import org.sonar.plugins.java.api.tree.CompilationUnitTree;
import org.sonar.plugins.java.api.tree.ConditionalExpressionTree;
import org.sonar.plugins.java.api.tree.ContinueStatementTree;
import org.sonar.plugins.java.api.tree.ExpressionTree;
import org.sonar.plugins.java.api.tree.ForStatementTree;
import org.sonar.plugins.java.api.tree.IdentifierTree;
import org.sonar.plugins.java.api.tree.IfStatementTree;
import org.sonar.plugins.java.api.tree.ImportTree;
import org.sonar.plugins.java.api.tree.LabeledStatementTree;
import org.sonar.plugins.java.api.tree.LiteralTree;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.ParenthesizedTree;
import org.sonar.plugins.java.api.tree.PrimitiveTypeTree;
import org.sonar.plugins.java.api.tree.StatementTree;
import org.sonar.plugins.java.api.tree.SyntaxToken;
import org.sonar.plugins.java.api.tree.SyntaxTrivia;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.TypeArguments;
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

  public static Set<String> allKindNames() {
    return Arrays.stream(UastNode.Kind.values()).map(UastNode.Kind::name).collect(Collectors.toSet());
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
      return visitToken(tree);
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

  private Stream<UastNode> visitToken(Tree tree) {
    UastNode uastNode = newUastNode(tree, Collections.emptyList());
    if (uastNode.token != null && SourceVersion.isKeyword(uastNode.token.value)) {
      uastNode.kinds.add(UastNode.Kind.KEYWORD);
    }
    if (((InternalSyntaxToken) tree).isEOF()) {
      uastNode.kinds.add(UastNode.Kind.EOF);
    }
    treeUastNodeMap.put(tree, uastNode);
    List<UastNode> trivia = ((SyntaxToken) tree).trivias().stream()
      // SonarJava AST duplicates some nodes (e.g. Variable)
      .filter(seenTrivia::add)
      .map(syntaxTrivia -> newUastNode(syntaxTrivia, Collections.emptyList()))
      .collect(Collectors.toList());
    return Stream.concat(trivia.stream(), Stream.of(uastNode));
  }

  private static UastNode newUastNode(Tree tree, List<UastNode> children) {
    return new UastNode(
      uastKind(tree),
      tree.kind().name(),
      tree.is(Tree.Kind.TOKEN, Tree.Kind.TRIVIA) ? newToken(tree) : null,
      children);
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
      text);
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
      case INTERFACE:
        result.add(UastNode.Kind.CLASS);
        result.add(UastNode.Kind.TYPE);
        break;
      case ENUM:
        result.add(UastNode.Kind.ENUM);
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
        result.add(UastNode.Kind.STRING_LITERAL);
        break;
      case FOR_EACH_STATEMENT:
        result.add(UastNode.Kind.FOREACH);
        break;
      case FOR_STATEMENT:
        result.add(UastNode.Kind.FOR);
        break;
      case WHILE_STATEMENT:
        result.add(UastNode.Kind.WHILE);
        break;
      case DO_STATEMENT:
        result.add(UastNode.Kind.DO_WHILE);
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
        if (((SyntaxTrivia) tree).comment().startsWith("/*")) {
          result.add(UastNode.Kind.STRUCTURED_COMMENT);
        }
        break;
      case BOOLEAN_LITERAL:
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
        result.add(UastNode.Kind.UNARY_EXPRESSION);
        break;
      case UNARY_PLUS:
        result.add(UastNode.Kind.UNARY_PLUS);
        result.add(UastNode.Kind.UNARY_EXPRESSION);
        break;
      case LOGICAL_COMPLEMENT:
        result.add(UastNode.Kind.LOGICAL_COMPLEMENT);
        result.add(UastNode.Kind.UNARY_EXPRESSION);
        break;
      case BITWISE_COMPLEMENT:
        result.add(UastNode.Kind.BITWISE_COMPLEMENT);
        result.add(UastNode.Kind.UNARY_EXPRESSION);
        break;
      case PREFIX_DECREMENT:
        result.add(UastNode.Kind.PREFIX_DECREMENT);
        result.add(UastNode.Kind.UNARY_EXPRESSION);
        break;
      case PREFIX_INCREMENT:
        result.add(UastNode.Kind.PREFIX_INCREMENT);
        result.add(UastNode.Kind.UNARY_EXPRESSION);
        break;
      case POSTFIX_DECREMENT:
        result.add(UastNode.Kind.POSTFIX_DECREMENT);
        result.add(UastNode.Kind.UNARY_EXPRESSION);
        break;
      case POSTFIX_INCREMENT:
        result.add(UastNode.Kind.POSTFIX_INCREMENT);
        result.add(UastNode.Kind.UNARY_EXPRESSION);
        break;
      case ANNOTATION:
        result.add(UastNode.Kind.ANNOTATION);
        break;
      case ANNOTATION_TYPE:
        result.add(UastNode.Kind.CLASS);
        result.add(UastNode.Kind.ANNOTATION_TYPE);
        result.add(UastNode.Kind.TYPE);
        break;
      case ARGUMENTS:
        result.add(UastNode.Kind.ARGUMENTS);
        break;
      case CATCH:
        result.add(UastNode.Kind.CATCH);
        break;
      case CONDITIONAL_EXPRESSION:
        result.add(UastNode.Kind.CONDITIONAL_EXPRESSION);
        break;
      case ASSERT_STATEMENT:
        result.add(UastNode.Kind.ASSERT);
        break;
      case EMPTY_STATEMENT:
        result.add(UastNode.Kind.EMPTY_STATEMENT);
        break;
      case TYPE_ARGUMENTS:
        result.add(UastNode.Kind.TYPE_ARGUMENTS);
        break;
      case TYPE_PARAMETERS:
        result.add(UastNode.Kind.TYPE_PARAMETERS);
        break;
      case TYPE_PARAMETER:
        result.add(UastNode.Kind.TYPE_PARAMETER);
        break;
      case CHAR_LITERAL:
        result.add(UastNode.Kind.CHAR_LITERAL);
        break;
      case NULL_LITERAL:
        result.add(UastNode.Kind.NULL_LITERAL);
        break;
      case INT_LITERAL:
      case LONG_LITERAL:
        String value = ((LiteralTree) tree).value().toLowerCase(Locale.ROOT);
        if (value.startsWith("0x")) {
          result.add(UastNode.Kind.HEX_LITERAL);
        } else if (value.startsWith("0b")) {
          result.add(UastNode.Kind.BINARY_LITERAL);
        } else if (value.startsWith("0") && value.length() > 1) {
          result.add(UastNode.Kind.OCTAL_LITERAL);
        } else {
          result.add(UastNode.Kind.DECIMAL_LITERAL);
        }
        break;
      case DOUBLE_LITERAL:
      case FLOAT_LITERAL:
        result.add(UastNode.Kind.FLOAT_LITERAL);
        break;
      case ARRAY_ACCESS_EXPRESSION:
        result.add(UastNode.Kind.ARRAY_ACCESS_EXPRESSION);
        break;
      case ARRAY_TYPE:
        result.add(UastNode.Kind.TYPE);
        break;
      case TYPE_CAST:
        result.add(UastNode.Kind.CAST);
        break;
      case ENUM_CONSTANT:
        result.add(UastNode.Kind.CONSTANT_DECLARATION);
        break;
      case IMPORT:
        result.add(UastNode.Kind.IMPORT);
        break;
      case INITIALIZER:
        result.add(UastNode.Kind.INITIALIZER);
        break;
      case VARIABLE:
        result.add(UastNode.Kind.VARIABLE_DECLARATION);
        break;
      case INSTANCE_OF:
        result.add(UastNode.Kind.TYPE_TEST);
        break;
      case MEMBER_SELECT:
        result.add(UastNode.Kind.MEMBER_SELECT);
        break;
      case METHOD_INVOCATION:
      case NEW_CLASS:
        result.add(UastNode.Kind.CALL);
        break;
      case PACKAGE:
        result.add(UastNode.Kind.PACKAGE);
        break;
      case PARAMETERIZED_TYPE:
      case PRIMITIVE_TYPE:
      case UNION_TYPE:
        result.add(UastNode.Kind.TYPE);
        break;
      case TRY_STATEMENT:
        result.add(UastNode.Kind.TRY);
        break;
      case MULTIPLY:
        result.add(UastNode.Kind.BINARY_EXPRESSION);
        result.add(UastNode.Kind.MULTIPLY);
        break;
      case DIVIDE:
        result.add(UastNode.Kind.BINARY_EXPRESSION);
        result.add(UastNode.Kind.DIVIDE);
        break;
      case REMAINDER:
        result.add(UastNode.Kind.BINARY_EXPRESSION);
        result.add(UastNode.Kind.REMAINDER);
        break;
      case PLUS:
        result.add(UastNode.Kind.BINARY_EXPRESSION);
        result.add(UastNode.Kind.ADD);
        break;
      case MINUS:
        result.add(UastNode.Kind.BINARY_EXPRESSION);
        result.add(UastNode.Kind.SUBTRACT);
        break;
      case LEFT_SHIFT:
        result.add(UastNode.Kind.BINARY_EXPRESSION);
        result.add(UastNode.Kind.LEFT_SHIFT);
        break;
      case RIGHT_SHIFT:
        result.add(UastNode.Kind.BINARY_EXPRESSION);
        result.add(UastNode.Kind.RIGHT_SHIFT);
        break;
      case UNSIGNED_RIGHT_SHIFT:
        result.add(UastNode.Kind.BINARY_EXPRESSION);
        break;
      case LESS_THAN:
        result.add(UastNode.Kind.BINARY_EXPRESSION);
        result.add(UastNode.Kind.LESS_THAN);
        break;
      case GREATER_THAN:
        result.add(UastNode.Kind.BINARY_EXPRESSION);
        result.add(UastNode.Kind.GREATER_THAN);
        break;
      case LESS_THAN_OR_EQUAL_TO:
        result.add(UastNode.Kind.BINARY_EXPRESSION);
        result.add(UastNode.Kind.LESS_OR_EQUAL);
        break;
      case GREATER_THAN_OR_EQUAL_TO:
        result.add(UastNode.Kind.BINARY_EXPRESSION);
        result.add(UastNode.Kind.GREATER_OR_EQUAL);
        break;
      case EQUAL_TO:
        result.add(UastNode.Kind.BINARY_EXPRESSION);
        result.add(UastNode.Kind.EQUAL);
        break;
      case NOT_EQUAL_TO:
        result.add(UastNode.Kind.BINARY_EXPRESSION);
        result.add(UastNode.Kind.NOT_EQUAL);
        break;
      case AND:
        result.add(UastNode.Kind.BINARY_EXPRESSION);
        result.add(UastNode.Kind.BITWISE_AND);
        break;
      case XOR:
        result.add(UastNode.Kind.BINARY_EXPRESSION);
        result.add(UastNode.Kind.BITWISE_XOR);
        break;
      case OR:
        result.add(UastNode.Kind.BINARY_EXPRESSION);
        result.add(UastNode.Kind.BITWISE_OR);
        break;
      case CONDITIONAL_AND:
        result.add(UastNode.Kind.BINARY_EXPRESSION);
        result.add(UastNode.Kind.LOGICAL_AND);
        break;
      case CONDITIONAL_OR:
        result.add(UastNode.Kind.BINARY_EXPRESSION);
        result.add(UastNode.Kind.LOGICAL_OR);
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
    public void visitBinaryExpression(BinaryExpressionTree tree) {
      addKind(tree.leftOperand(), UastNode.Kind.LEFT_OPERAND);
      addKind(tree.operatorToken(), UastNode.Kind.OPERATOR);
      addKind(tree.rightOperand(), UastNode.Kind.RIGHT_OPERAND);
      super.visitBinaryExpression(tree);
    }

    @Override
    public void visitUnaryExpression(UnaryExpressionTree tree) {
      addKind(tree.operatorToken(), UastNode.Kind.OPERATOR);
      addKind(tree.expression(), UastNode.Kind.OPERAND);
      super.visitUnaryExpression(tree);
    }

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
      addKind(tree.simpleName(), UastNode.Kind.VARIABLE_NAME);
      addKind(tree.initializer(), UastNode.Kind.INITIALIZER);
      addKind(tree.type(), UastNode.Kind.TYPE);
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

      // in SonarJava parameters are direct children of MethodTree, in UAST we want to have PARAMETER_LIST node to be child of FUNCTION node
      UastNode methodNode = treeUastNodeMap.get(tree);
      List<UastNode> paramListChildren = new ArrayList<>();
      UastNode openParen = treeUastNodeMap.get(tree.openParenToken());
      int openParenPos = methodNode.children.indexOf(openParen);
      methodNode.children.remove(openParen);
      paramListChildren.add(openParen);
      tree.parameters().forEach(p -> {
        addKind(p, UastNode.Kind.PARAMETER);
        UastNode paramNode = treeUastNodeMap.get(p);
        methodNode.children.remove(paramNode);
        paramListChildren.add(paramNode);
      });
      UastNode closeParen = treeUastNodeMap.get(tree.closeParenToken());
      methodNode.children.remove(closeParen);
      paramListChildren.add(closeParen);
      UastNode paramListNode = new UastNode(Collections.singleton(UastNode.Kind.PARAMETER_LIST), "List", null, paramListChildren);
      methodNode.children.add(openParenPos, paramListNode);
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

    @Override
    public void visitConditionalExpression(ConditionalExpressionTree tree) {
      addKind(tree.condition(), UastNode.Kind.CONDITION);
      addKind(tree.trueExpression(), UastNode.Kind.THEN);
      addKind(tree.falseExpression(), UastNode.Kind.ELSE);
      super.visitConditionalExpression(tree);
    }

    @Override
    public void visitMethodInvocation(MethodInvocationTree tree) {
      tree.arguments().forEach(arg -> addKind(arg, UastNode.Kind.ARGUMENT));
      super.visitMethodInvocation(tree);
    }

    @Override
    public void visitTypeArguments(TypeArguments trees) {
      trees.forEach(typeArg -> addKind(typeArg, UastNode.Kind.TYPE_ARGUMENT));
      super.visitTypeArguments(trees);
    }

    @Override
    public void visitBreakStatement(BreakStatementTree tree) {
      addKind(tree.label(), UastNode.Kind.BRANCH_LABEL);
      super.visitBreakStatement(tree);
    }

    @Override
    public void visitContinueStatement(ContinueStatementTree tree) {
      // bug in the parser, see SONARJAVA-2723
      IdentifierTree labelTree = tree.label();
      if (labelTree != null) {
        UastNode continueNode = treeUastNodeMap.get(tree);
        UastNode label = newUastNode(labelTree, Collections.emptyList());
        continueNode.children.add(label);
        treeUastNodeMap.put(labelTree, label);
        addKind(labelTree, UastNode.Kind.BRANCH_LABEL);
      }
      super.visitContinueStatement(tree);
    }

    @Override
    public void visitLabeledStatement(LabeledStatementTree tree) {
      addKind(tree.label(), UastNode.Kind.LABEL);
      super.visitLabeledStatement(tree);
    }

    @Override
    public void visitForStatement(ForStatementTree tree) {
      addKind(tree.forKeyword(), UastNode.Kind.FOR_KEYWORD);
      addKind(tree.initializer(), UastNode.Kind.FOR_INIT);
      addKind(tree.update(), UastNode.Kind.FOR_UPDATE);
      addKind(tree.statement(), UastNode.Kind.BODY);
      super.visitForStatement(tree);
    }

    @Override
    public void visitArrayAccessExpression(ArrayAccessExpressionTree tree) {
      addKind(tree.expression(), UastNode.Kind.ARRAY_OBJECT_EXPRESSION);
      addKind(tree.dimension().expression(), UastNode.Kind.ARRAY_KEY_EXPRESSION);
      super.visitArrayAccessExpression(tree);
    }

    @Override
    public void visitImport(ImportTree tree) {
      addKind(tree.qualifiedIdentifier(), UastNode.Kind.IMPORT_ENTRY);
      super.visitImport(tree);
    }

    private void addKind(@Nullable Tree tree, UastNode.Kind... kind) {
      if (tree == null) {
        return;
      }
      UastNode uastNode = treeUastNodeMap.get(tree);
      if (uastNode != null) {
        uastNode.kinds.addAll(Arrays.asList(kind));
      }
    }
  }

  static class GeneratorException extends RuntimeException {
    public GeneratorException(Throwable cause) {
      super(cause);
    }
  }
}
