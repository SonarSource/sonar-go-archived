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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.sonar.java.ast.parser.JavaParser;
import org.sonar.java.model.JavaTree;
import org.sonar.plugins.java.api.tree.AssignmentExpressionTree;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.CompilationUnitTree;
import org.sonar.plugins.java.api.tree.ExpressionTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.StatementTree;
import org.sonar.plugins.java.api.tree.SyntaxToken;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.VariableTree;

public class Generator {

  public static void main(String[] args) throws IOException {
    Path path = Paths.get(args[0]);
    if (Files.isDirectory(path)) {
      Files.walk(path)
        .filter(p -> p.toString().endsWith(".java"))
        .forEach(Generator::createUastFile);
    } else {
      Generator.createUastFile(path);
    }
  }

  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final ActionParser<Tree> PARSER = JavaParser.createParser();
  private final CompilationUnitTree cut;
  private final UastNode uast;
  private Map<Tree, UastNode> treeUastNodeMap = new HashMap<>();

  public Generator(String source) {
    cut = (CompilationUnitTree) PARSER.parse(source);
    uast = visit(cut);
    cut.accept(new PostprocessVisitor());
  }

  private static String readFile(Path path) {
    try {
      return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static void createUastFile(Path p) {
    try {
      String source = readFile(p);
      String uast = new Generator(source).json();
      Files.write(Paths.get(p.toString() + ".uast.json"), uast.getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public UastNode uast() {
    return uast;
  }

  public String json() {
    return GSON.toJson(uast);
  }

  private UastNode visit(Tree tree) {
    UastNode uast = null;
    if (tree.is(Tree.Kind.TOKEN)) {
      uast = newUastNode(tree, Collections.emptyList());
    } else {
      List<Tree> children = ((JavaTree) tree).getChildren();
      if (!children.isEmpty()) {
        uast = newUastNode(tree, children.stream().map(this::visit).filter(Objects::nonNull).collect(Collectors.toList()));
      }
    }
    if (uast != null) {
      treeUastNodeMap.put(tree, uast);
    }
    return uast;
  }

  private static UastNode newUastNode(Tree tree, List<UastNode> children) {
    return new UastNode(
            uastKind(tree),
            tree.kind().name(),
            tree.is(Tree.Kind.TOKEN) ? newToken((SyntaxToken) tree) : null,
            children
    );
  }

  private static UastNode.Token newToken(SyntaxToken javaToken) {
    return new UastNode.Token(
            javaToken.line(),
            // as per UAST specification column starts at 1
            javaToken.column() + 1,
            javaToken.text()
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
      case LAMBDA_EXPRESSION:
        result.add(UastNode.Kind.FUNCTION);
        break;
      case CLASS:
      case ENUM:
      case INTERFACE:
      case ANNOTATION_TYPE:
        result.add(UastNode.Kind.CLASS);
        break;
      case ASSIGNMENT:
        result.add(UastNode.Kind.ASSIGNMENT);
        break;
      case BLOCK:
        result.add(UastNode.Kind.BLOCK);
        break;
      case IDENTIFIER:
        result.add(UastNode.Kind.IDENTIFIER);
        break;
      case STRING_LITERAL:
        result.add(UastNode.Kind.LITERAL);
        break;
      default:
        break;
    }
    if (tree instanceof StatementTree) {
      result.add(UastNode.Kind.STATEMENT);
    }
    return result;
  }


  class PostprocessVisitor extends BaseTreeVisitor {

    @Override
    public void visitAssignmentExpression(AssignmentExpressionTree tree) {
      ExpressionTree variable = tree.variable();
      ExpressionTree expression = tree.expression();
      addKind(variable, UastNode.Kind.ASSIGNMENT_TARGET);
      addKind(expression, UastNode.Kind.ASSIGNMENT_VALUE);
      super.visitAssignmentExpression(tree);
    }

    @Override
    public void visitVariable(VariableTree tree) {
      super.visitVariable(tree);
      if (tree.initializer() != null) {
        addKind(tree, UastNode.Kind.ASSIGNMENT);
        addKind(tree.simpleName(), UastNode.Kind.ASSIGNMENT_TARGET);
        addKind(tree.initializer(), UastNode.Kind.ASSIGNMENT_VALUE);
      }
    }

    @Override
    public void visitMethod(MethodTree tree) {
      tree.parameters().forEach(p -> treeUastNodeMap.get(p).kinds.add(UastNode.Kind.PARAMETER));
      super.visitMethod(tree);
    }
  }

  private void addKind(Tree tree, UastNode.Kind kind) {
    treeUastNodeMap.get(tree).kinds.add(kind);
  }

}
