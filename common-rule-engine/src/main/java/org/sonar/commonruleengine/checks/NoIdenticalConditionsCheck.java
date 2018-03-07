package org.sonar.commonruleengine.checks;

import org.sonar.check.Rule;
import org.sonar.uast.UastNode;
import org.sonar.uast.helpers.IfLike;

import static org.sonar.uast.Uast.syntacticallyEquivalent;

/**
 * https://jira.sonarsource.com/browse/RSPEC-1862
 */
@Rule(key = "S1862")
public class NoIdenticalConditionsCheck extends Check {

  public NoIdenticalConditionsCheck() {
    super(UastNode.Kind.IF);
  }

  @Override
  public void visitNode(UastNode node) {
    IfLike ifLike = IfLike.from(node);
    if (ifLike == null) {
      return;
    }
    UastNode condition = ifLike.condition();
    IfLike elseIf = IfLike.from(ifLike.elseNode());
    while (elseIf != null) {
      if (syntacticallyEquivalent(condition, elseIf.condition())) {
        reportIssue(elseIf.condition(), "Unreachable branch because of the same condition before. " + condition.joinTokens());
      }
      elseIf = IfLike.from(elseIf.elseNode());
    }
  }
}
