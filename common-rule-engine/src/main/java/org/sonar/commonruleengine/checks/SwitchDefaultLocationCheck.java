package org.sonar.commonruleengine.checks;

import java.util.List;
import org.sonar.check.Rule;
import org.sonar.uast.UastNode;
import org.sonar.uast.helpers.SwitchLike;

/**
 * https://jira.sonarsource.com/browse/RSPEC-4524
 */
@Rule(key = "S4524")
public class SwitchDefaultLocationCheck extends Check {

  public SwitchDefaultLocationCheck() {
    super(UastNode.Kind.SWITCH);
  }

  @Override
  public void visitNode(UastNode node) {
    List<UastNode> caseNodes = SwitchLike.from(node).caseNodes();
    caseNodes.stream().filter(UastNode.Kind.DEFAULT_CASE).findFirst().ifPresent(defaultCase -> {
      int index = caseNodes.indexOf(defaultCase);
      if (index != 0 && index != caseNodes.size() - 1) {
        reportIssue(defaultCase, "Move this \"default\" case clause to the beginning or end of this \"switch\" statement.");
      }
    });
  }

}
