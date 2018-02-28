package org.sonar.commonruleengine.checks;

import java.util.Collections;
import java.util.List;
import org.sonar.uast.UastNode;

/**
 * Rule https://jira.sonarsource.com/browse/RSPEC-4144
 */
public class NoIdenticalFunctionsCheck extends Check {

  @Override
  public List<UastNode.Kind> nodeKindsToVisit() {
    return Collections.singletonList(UastNode.Kind.FUNCTION);
  }

  @Override
  public void visitNode(UastNode node) {
    // dummy implementation which reports every method
    reportIssue(node, "Issue here");
  }
}
