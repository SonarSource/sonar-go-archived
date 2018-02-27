package org.sonar.commonruleengine.checks;

import org.sonar.uast.Uast;
import org.sonar.uast.UastNode;
import org.sonar.uast.helpers.AssignmentLike;

/**
 * https://jira.sonarsource.com/browse/RSPEC-1656
 */
public class NoSelfAssignmentCheck extends CommonCheck {

  @Override
  public void visitNode(UastNode node) {
    if (node.kinds.contains(UastNode.Kind.ASSIGNMENT)) {
      AssignmentLike assignment = new AssignmentLike(node);
      if (Uast.syntacticallyEquivalent(assignment.target(), assignment.value())) {
        reportIssue(node, "Remove this self assignment");
      }
    }
  }
}


