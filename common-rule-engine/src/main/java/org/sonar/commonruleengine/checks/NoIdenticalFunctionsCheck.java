package org.sonar.commonruleengine.checks;

import org.sonar.uast.UastNode;

/**
 * Rule https://jira.sonarsource.com/browse/RSPEC-4144
 */
public class NoIdenticalFunctionsCheck extends Check {

  public NoIdenticalFunctionsCheck() {
    super(UastNode.Kind.FUNCTION);
  }

  @Override
  public void visitNode(UastNode node) {
    // dummy implementation which reports every method
    reportIssue(node, "Issue here");
  }
}
