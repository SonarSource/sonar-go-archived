package org.sonar.uast.generator.java;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sonar.sslr.api.typed.ActionParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.sonar.java.ast.parser.JavaParser;
import org.sonar.java.model.JavaTree;
import org.sonar.plugins.java.api.tree.AssignmentExpressionTree;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.CompilationUnitTree;
import org.sonar.plugins.java.api.tree.ExpressionTree;
import org.sonar.plugins.java.api.tree.SyntaxToken;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.uast.UastNode;

public class Generator {

  public static void main(String[] args) throws IOException {
    System.out.println(new Generator(fileContent(args[0])).json());
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

  static String fileContent(String fileLocation) throws IOException {
    byte[] bytes = Files.readAllBytes(Paths.get(fileLocation));
    return new String(bytes, StandardCharsets.UTF_8);
  }

  private UastNode visit(Tree tree) {
    UastNode uast = newUastNode(tree);
    treeUastNodeMap.put(tree, uast);
    if (!tree.is(Tree.Kind.TOKEN)) {
      uast.children = ((JavaTree) tree).getChildren().stream().map(this::visit).collect(Collectors.toList());
    }
    return uast;
  }

  private static UastNode newUastNode(Tree tree) {
    UastNode result = new UastNode();
    result.nativeNode = tree.kind().name();
    uastKind(tree).ifPresent(kind -> result.kinds = EnumSet.of(kind));
    if (tree.is(Tree.Kind.TOKEN)) {
      result.token = newToken((SyntaxToken) tree);
    }
    return result;
  }

  private static UastNode.Token newToken(SyntaxToken javaToken) {
    UastNode.Token result = new UastNode.Token();
    result.column = javaToken.column();
    result.line = javaToken.line();
    result.value = javaToken.text();
    return result;
  }

  private static Optional<UastNode.Kind> uastKind(Tree tree) {
    UastNode.Kind result;
    switch (tree.kind()) {
      case COMPILATION_UNIT:
        result = UastNode.Kind.COMPILATION_UNIT;
        break;
      case METHOD:
      case CONSTRUCTOR:
      case LAMBDA_EXPRESSION:
        result = UastNode.Kind.FUNCTION;
        break;
      case CLASS:
      case ENUM:
      case INTERFACE:
      case ANNOTATION_TYPE:
        result = UastNode.Kind.CLASS;
        break;
      case ASSIGNMENT:
        result = UastNode.Kind.ASSIGNMENT;
        break;
      default:
        result = null;
    }
    return Optional.ofNullable(result);
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
  }

}
