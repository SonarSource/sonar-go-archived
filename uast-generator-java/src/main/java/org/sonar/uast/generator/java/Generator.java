package org.sonar.uast.generator.java;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sonar.sslr.api.typed.ActionParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
import org.sonar.uast.UastNode;

public class Generator {

  public static void main(String[] args) throws IOException {
    String source = new String(Files.readAllBytes(Paths.get(args[0])), StandardCharsets.UTF_8);
    System.out.println(new Generator(source).json());
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
      treeUastNodeMap.get(variable).kinds.add(UastNode.Kind.ASSIGNMENT_TARGET);
      treeUastNodeMap.get(expression).kinds.add(UastNode.Kind.ASSIGNMENT_VALUE);
      super.visitAssignmentExpression(tree);
    }

    @Override
    public void visitMethod(MethodTree tree) {
      tree.parameters().forEach(p -> treeUastNodeMap.get(p).kinds.add(UastNode.Kind.PARAMETER));
      super.visitMethod(tree);
    }
  }

}
