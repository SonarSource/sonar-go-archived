package org.sonar.go.plugin;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.cpd.NewCpdTokens;
import org.sonar.uast.UastNode;

import static org.sonar.go.plugin.utils.PluginApiUtils.newRange;

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

      cpdTokens.addToken(newRange(inputFile, token), text);
    }

    for (UastNode child : node.children) {
      scan(child);
    }
  }

  public void save() {
    cpdTokens.save();
  }

}
