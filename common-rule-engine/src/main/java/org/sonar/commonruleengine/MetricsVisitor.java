package org.sonar.commonruleengine;

import java.util.Set;
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
    UastNode.Token token = node.token;
    if (token != null) {
      visitToken(node.kinds, token);
    }
  }

  public void visitToken(Set<UastNode.Kind> nodeKinds, UastNode.Token token) {
    Set<Integer> lineNumbers = nodeKinds.contains(UastNode.Kind.COMMENT) ? metrics.commentLines : metrics.linesOfCode;
    for (int line = token.line; line <= token.endLine; line++) {
      lineNumbers.add(line);
    }
  }

  public Metrics getMetrics() {
    return metrics;
  }

}
