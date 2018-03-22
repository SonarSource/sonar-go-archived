package org.sonar.commonruleengine.checks;

import java.util.ArrayList;
import java.util.List;
import org.sonar.check.Rule;
import org.sonar.commonruleengine.Issue;
import org.sonar.uast.UastNode;
import org.sonar.uast.helpers.CaseLike;
import org.sonar.uast.helpers.IfLike;
import org.sonar.uast.helpers.SwitchLike;

import static org.sonar.uast.Uast.syntacticallyEquivalent;

/**
 * https://jira.sonarsource.com/browse/RSPEC-1862
 */
@Rule(key = "S1862")
public class NoIdenticalConditionsCheck extends Check {

  public NoIdenticalConditionsCheck() {
    super(UastNode.Kind.IF, UastNode.Kind.SWITCH);
  }

  @Override
  public void visitNode(UastNode node) {
    handleIf(node);
    handleSwitch(node);
  }

  private void handleSwitch(UastNode node) {
    SwitchLike switchLike = SwitchLike.from(node);
    if (switchLike == null) {
      return;
    }
    List<UastNode> caseNodes = switchLike.caseNodes();
    List<UastNode> allConditions = new ArrayList<>();
    for (UastNode caseNode : caseNodes) {
      CaseLike caseLike = CaseLike.from(caseNode);
      List<UastNode> conditions = caseLike.conditions();
      for (UastNode condition : conditions) {
        for (UastNode prevCondition : allConditions) {
          if (syntacticallyEquivalent(condition, prevCondition)) {
            int line = condition.firstToken().line;
            reportIssue(condition, "This condition is same as one already tested on line " + line + ".",
              new Issue.Message(prevCondition, "Original"));
          }
        }
        allConditions.add(condition);
      }
    }
  }

  private void handleIf(UastNode node) {
    IfLike ifLike = IfLike.from(node);
    if (ifLike == null) {
      return;
    }
    UastNode condition = ifLike.condition();
    IfLike elseIf = IfLike.from(ifLike.elseNode());
    while (elseIf != null) {
      if (syntacticallyEquivalent(condition, elseIf.condition())) {
        int line = condition.firstToken().line;
        reportIssue(elseIf.condition(), "This condition is same as one already tested on line " + line + ".",
          new Issue.Message(condition, "Original"));
      }
      elseIf = IfLike.from(elseIf.elseNode());
    }
  }
}
