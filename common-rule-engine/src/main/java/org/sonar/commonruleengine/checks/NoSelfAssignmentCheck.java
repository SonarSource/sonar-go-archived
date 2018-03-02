package org.sonar.commonruleengine.checks;

import org.sonar.check.Rule;
import org.sonar.uast.UastNode;
import org.sonar.uast.helpers.AssignmentLike;

import static org.sonar.uast.Uast.syntacticallyEquivalent;

/**
 * https://jira.sonarsource.com/browse/RSPEC-1656
 */
@Rule(key = "S1656")
public class NoSelfAssignmentCheck extends Check {

  public NoSelfAssignmentCheck() {
    super(AssignmentLike.KIND);
  }

  @Override
  public void visitNode(UastNode node) {
    AssignmentLike assignment = AssignmentLike.from(node);
    if (assignment != null && syntacticallyEquivalent(assignment.target(), assignment.value())) {
      reportIssue(node, "Remove this self assignment");
    }
  }
}
