package org.sonar.commonruleengine.checks;

import org.sonar.check.Rule;
import org.sonar.uast.UastNode;
import org.sonar.uast.helpers.SwitchLike;

/**
 * https://jira.sonarsource.com/browse/RSPEC-131
 */
@Rule(key = "S131")
public class SwitchWithoutDefaultCheck extends Check {

  public SwitchWithoutDefaultCheck() {
    super(UastNode.Kind.SWITCH);
  }

  @Override
  public void visitNode(UastNode node) {
    SwitchLike switchLike = SwitchLike.from(node);
    if (switchLike != null && switchLike.caseNodes().stream().noneMatch(UastNode.Kind.DEFAULT_CASE)) {
      reportIssue(node, "Add a default case to this switch.");
    }
  }

}
