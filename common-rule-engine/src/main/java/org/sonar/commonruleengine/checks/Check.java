package org.sonar.commonruleengine.checks;

import java.util.List;
import org.sonar.commonruleengine.EngineContext;
import org.sonar.commonruleengine.Issue;
import org.sonar.uast.UastNode;

public abstract class Check {

  protected EngineContext context;

  public void initialize(EngineContext context) {
    this.context = context;
    nodeKindsToVisit().forEach(kind -> context.register(kind, this));
  }

  protected abstract List<UastNode.Kind> nodeKindsToVisit();

  public abstract void visitNode(UastNode node);

  protected final void reportIssue(UastNode node, String message) {
    context.reportIssue(new Issue(this, node, message));
  }
}
