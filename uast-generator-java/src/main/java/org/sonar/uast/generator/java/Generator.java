package org.sonar.uast.generator.java;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sonar.sslr.api.typed.ActionParser;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Collectors;
import org.sonar.api.internal.google.common.io.Files;
import org.sonar.java.ast.parser.JavaParser;
import org.sonar.java.model.JavaTree;
import org.sonar.plugins.java.api.tree.CompilationUnitTree;
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

  public Generator(String source) {
    cut = (CompilationUnitTree) PARSER.parse(source);
    uast = visit(cut);
  }

  public UastNode uast() {
    return uast;
  }

  public String json() {
    return GSON.toJson(uast);
  }

  static String fileContent(String fileLocation) throws IOException {
    return Files.readLines(new File(fileLocation), StandardCharsets.UTF_8).stream().collect(Collectors.joining("\n"));
  }

  private static UastNode visit(Tree tree) {
    UastNode uast = newUastNode(tree);
    if (!tree.is(Tree.Kind.TOKEN)) {
      ((JavaTree) tree).getChildren().forEach(child -> uast.children.add(visit(child)));
    }
    return uast;
  }

  private static UastNode newUastNode(Tree tree) {
    UastNode result = new UastNode();
    result.nativeNode = tree.kind().name();
    uastKind(tree).ifPresent(result.kinds::add);
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
    UastNode.Kind result = null;
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
      default:
        result = null;
    }
    return Optional.ofNullable(result);
  }

}
