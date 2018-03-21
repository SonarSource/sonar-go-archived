package org.sonar.commonruleengine;

import java.util.Set;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import org.sonar.uast.UastNode;

public class MetricsVisitor {

  private Metrics metrics;

  public void enterFile() {
    metrics = new Metrics();
  }

  public void visitNode(UastNode node) {
    if (node.kinds.contains(UastNode.Kind.CLASS)) {
      metrics.numberOfClasses++;
    }
    if (node.kinds.contains(UastNode.Kind.FUNCTION)) {
      metrics.numberOfFunctions++;
    }
    if (node.kinds.contains(UastNode.Kind.STATEMENT)) {
      metrics.numberOfStatements++;
    }
    if (node.kinds.contains(UastNode.Kind.STATEMENT) ||
      node.kinds.contains(UastNode.Kind.EXPRESSION) ||
      node.kinds.contains(UastNode.Kind.CASE) ||
      node.kinds.contains(UastNode.Kind.LABEL)) {
      addLines(metrics.executableLines, node.firstToken());
    }
    UastNode.Token token = node.token;
    if (token != null) {
      visitToken(node.kinds, token);
    }
  }

  public void visitToken(Set<UastNode.Kind> nodeKinds, UastNode.Token token) {
    if (nodeKinds.contains(UastNode.Kind.EOF)) {
      return;
    }
    Set<Integer> lineNumbers = nodeKinds.contains(UastNode.Kind.COMMENT) ? metrics.commentLines : metrics.linesOfCode;
    addLines(lineNumbers, token);
  }

  private static void addLines(Set<Integer> lineNumbers, @Nullable UastNode.Token token) {
    if (token != null) {
      IntStream.range(token.line, token.endLine + 1).forEach(lineNumbers::add);
    }
  }

  public Metrics getMetrics() {
    return metrics;
  }

}
