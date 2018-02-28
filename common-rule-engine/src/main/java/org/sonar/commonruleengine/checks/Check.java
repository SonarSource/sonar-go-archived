package org.sonar.commonruleengine.checks;

import org.sonar.commonruleengine.EngineContext;
import org.sonar.commonruleengine.Issue;
import org.sonar.uast.UastNode;

public abstract class Check {

  protected EngineContext context;

  public void setContext(EngineContext context) {
    this.context = context;
  }

  public abstract void visitNode(UastNode node);

  protected final void reportIssue(UastNode node, String message) {
    context.reportIssue(new Issue(this, node, message));
  }
}
