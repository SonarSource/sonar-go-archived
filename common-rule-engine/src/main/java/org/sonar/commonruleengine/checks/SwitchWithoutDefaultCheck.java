package org.sonar.commonruleengine.checks;

import java.util.ArrayList;
import java.util.List;
import org.sonar.check.Rule;
import org.sonar.uast.UastNode;

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
    List<UastNode> casesNodes = getCases(node);
    if (casesNodes.stream().noneMatch(UastNode.Kind.DEFAULT_CASE)) {
      reportIssue(node, "Add a default case to this switch.");
    } else {
      int defaultCasePosition = getDefaultPosition(casesNodes);
      int lastPosition = casesNodes.size() - 1;
      if (defaultCasePosition != 0 && defaultCasePosition != lastPosition) {
        reportIssue(casesNodes.get(defaultCasePosition), "Move this default to the start or end of the switch.");
      }
    }
  }

  private static int getDefaultPosition(List<UastNode> casesNodes) {
    int result = 0;
    for (UastNode caseNode : casesNodes) {
      if (UastNode.Kind.DEFAULT_CASE.test(caseNode)) {
        break;
      }
      result++;
    }
    return result;
  }

  private static List<UastNode> getCases(UastNode switchNode) {
    List<UastNode> results = new ArrayList<>();
    switchNode.children.forEach(child -> child.getDescendants(UastNode.Kind.CASE, results::add, UastNode.Kind.SWITCH));
    return results;
  }

}
