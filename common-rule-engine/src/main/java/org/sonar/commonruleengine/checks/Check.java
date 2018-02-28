package org.sonar.commonruleengine.checks;

import java.util.Arrays;
import org.sonar.commonruleengine.EngineContext;
import org.sonar.commonruleengine.Issue;
import org.sonar.uast.UastNode;

public abstract class Check {

  private final UastNode.Kind[] kinds;
  protected EngineContext context;

  public Check(UastNode.Kind... nodeKindsToVisit) {
    this.kinds = nodeKindsToVisit;
  }

  public void initialize(EngineContext context) {
    this.context = context;
    Arrays.stream(kinds).forEach(kind -> context.register(kind, this));
  }

  public abstract void visitNode(UastNode node);

  protected final void reportIssue(UastNode node, String message) {
    context.reportIssue(new Issue(this, node, message));
  }
}
