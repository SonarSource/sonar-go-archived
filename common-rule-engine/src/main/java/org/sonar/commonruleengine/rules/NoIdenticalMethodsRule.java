package org.sonar.commonruleengine.rules;

import org.sonar.uast.UastNode;

/**
 * Rule https://jira.sonarsource.com/browse/RSPEC-4144
 */
public class NoIdenticalMethodsRule extends CommonRule {

  @Override
  public void visitNode(UastNode node) {
    if (node.kinds.contains("METHOD")) {
      // dummy implementation which reports every method
      reportIssue(node, "Issue here");
    }
  }
}
