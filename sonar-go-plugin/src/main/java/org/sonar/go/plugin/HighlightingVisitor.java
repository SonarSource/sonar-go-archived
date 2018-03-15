package org.sonar.go.plugin;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.highlighting.NewHighlighting;
import org.sonar.api.batch.sensor.highlighting.TypeOfText;
import org.sonar.uast.UastNode;
import org.sonar.uast.UastNode.Kind;

public class HighlightingVisitor {

  private final InputFile inputFile;
  private final NewHighlighting newHighlighting;

  public HighlightingVisitor(SensorContext sensorContext, InputFile inputFile) {
    this.inputFile = inputFile;
    newHighlighting = sensorContext.newHighlighting()
      .onFile(inputFile);
  }

  public void scan(UastNode node) {
    scanRecursively(node, false);
  }

  public void scanRecursively(UastNode node, boolean parentIsATypeDefinition) {
    UastNode.Token token = node.token;
    boolean isATypeDefinition = parentIsATypeDefinition || node.kinds.contains(Kind.TYPE);
    if (token != null) {
      if (node.kinds.contains(Kind.COMMENT)) {
        highlight(token, node.kinds.contains(Kind.STRUCTURED_COMMENT) ? TypeOfText.STRUCTURED_COMMENT : TypeOfText.COMMENT);
      } else if (node.kinds.contains(Kind.KEYWORD)) {
        highlight(token, TypeOfText.KEYWORD);
      } else if (node.kinds.contains(Kind.LITERAL)) {
        highlight(token, node.kinds.contains(Kind.STRING_LITERAL) ? TypeOfText.STRING : TypeOfText.CONSTANT);
      } else if (isATypeDefinition) {
        highlight(token, TypeOfText.KEYWORD_LIGHT);
      }
    }
    for (UastNode child : node.children) {
      scanRecursively(child, isATypeDefinition);
    }
  }

  private NewHighlighting highlight(UastNode.Token token, TypeOfText typeOfText) {
    return newHighlighting.highlight(textRange(token), typeOfText);
  }

  private TextRange textRange(UastNode.Token token) {
    return inputFile.newRange(
      token.line,
      token.column - 1,
      token.endLine,
      token.endColumn);
  }

  public void save() {
    newHighlighting.save();
  }

}
