package org.sonar.commonruleengine.checks;

import java.util.Collections;
import java.util.List;
import org.sonar.uast.UastNode;
import org.sonar.uast.helpers.AssignmentLike;

import static org.sonar.uast.Uast.syntacticallyEquivalent;

/**
 * https://jira.sonarsource.com/browse/RSPEC-1656
 */
public class NoSelfAssignmentCheck extends Check {

  @Override
  public List<UastNode.Kind> nodeKindsToVisit() {
    return Collections.singletonList(AssignmentLike.KIND);
  }

  @Override
  public void visitNode(UastNode node) {
    AssignmentLike assignment = AssignmentLike.from(node);
    if (assignment != null && syntacticallyEquivalent(assignment.target(), assignment.value())) {
      reportIssue(node, "Remove this self assignment");
    }
  }
}
