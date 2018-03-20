package org.sonar.go.plugin;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.cpd.NewCpdTokens;
import org.sonar.uast.UastNode;

public class CpdVisitor {

  private final InputFile inputFile;
  private final NewCpdTokens cpdTokens;

  CpdVisitor(SensorContext sensorContext, InputFile inputFile) {
    this.inputFile = inputFile;
    cpdTokens = sensorContext.newCpdTokens().onFile(inputFile);
  }

  public void scan(UastNode node) {
    if (node.kinds.contains(UastNode.Kind.COMMENT) || node.kinds.contains(UastNode.Kind.EOF)) {
      return;
    }

    UastNode.Token token = node.token;
    if (token != null) {
      String text = token.value;
      if (node.kinds.contains(UastNode.Kind.LITERAL)) {
        text = "LITERAL";
      }

      cpdTokens.addToken(textRange(token), text);
    }

    for (UastNode child : node.children) {
      scan(child);
    }
  }

  private TextRange textRange(UastNode.Token token) {
    return inputFile.newRange(
      token.line,
      token.column - 1,
      token.endLine,
      token.endColumn);
  }

  public void save() {
    cpdTokens.save();
  }

}
