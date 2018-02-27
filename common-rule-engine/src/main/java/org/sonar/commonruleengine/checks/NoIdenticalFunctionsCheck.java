package org.sonar.commonruleengine.checks;

import org.sonar.uast.UastNode;

/**
 * Rule https://jira.sonarsource.com/browse/RSPEC-4144
 */
public class NoIdenticalFunctionsCheck extends CommonCheck {

  @Override
  public void visitNode(UastNode node) {
    if (node.kinds.contains(UastNode.Kind.FUNCTION)) {
      // dummy implementation which reports every method
      reportIssue(node, "Issue here");
    }
  }
}
