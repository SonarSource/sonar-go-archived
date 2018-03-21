package org.sonar.go.plugin.utils;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.uast.UastNode;

public class PluginApiUtils {
  private PluginApiUtils() {
    // utility class, forbidden constructor
  }

  public static TextRange newRange(InputFile inputFile, UastNode.Token token) {
    return inputFile.newRange(
      token.line,
      token.column - 1,
      token.endLine,
      token.endColumn);
  }

  public static TextRange newRange(InputFile inputFile, UastNode node) {
    UastNode.Token firstToken = node.firstToken();
    UastNode.Token lastToken = node.lastToken();
    return inputFile.newRange(
      firstToken.line,
      firstToken.column - 1,
      lastToken.endLine,
      lastToken.endColumn);
  }

}
