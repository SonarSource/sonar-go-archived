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

  /**
   * This method is called only once by analysis
   */
  public void initialize(EngineContext context) {
    this.context = context;
    Arrays.stream(kinds).forEach(kind -> context.register(kind, this));
  }

  /**
   * This method is called every time we enter a new file, allowing state cleaning for checks
   */
  public void enterFile() {
  }

  public abstract void visitNode(UastNode node);

  /**
   * This method is called after "visitNode(node)" of the node itself and all its descendants
   */
  public void leaveNode(UastNode node) {
  }

  protected final void reportIssue(UastNode node, String message) {
    context.reportIssue(new Issue(this, new Issue.Message(node, message)));
  }

  protected final void reportIssue(UastNode node, String message, Issue.Message... secondaryMessages) {
    context.reportIssue(new Issue(this, new Issue.Message(node, message), secondaryMessages));
  }

  protected final void reportIssue(UastNode from, UastNode to, String message, Issue.Message... secondaryMessages) {
    context.reportIssue(new Issue(this, new Issue.Message(from, to, message), secondaryMessages));
  }
}
