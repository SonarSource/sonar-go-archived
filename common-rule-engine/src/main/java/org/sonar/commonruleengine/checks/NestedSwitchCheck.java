package org.sonar.commonruleengine.checks;

import java.util.HashSet;
import java.util.Set;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.check.Rule;
import org.sonar.uast.UastNode;
import org.sonar.uast.helpers.SwitchLike;

/**
 * https://jira.sonarsource.com/browse/RSPEC-1821
 */
@Rule(key = "S1821")
public class NestedSwitchCheck extends Check {

  private final Set<UastNode> reported = new HashSet<>();

  public NestedSwitchCheck() {
    super(UastNode.Kind.SWITCH);
  }

  @Override
  public void enterFile(InputFile inputFile) {
    reported.clear();
  }

  @Override
  public void visitNode(UastNode node) {
    reported.add(node);
    node.getDescendants(UastNode.Kind.SWITCH, this::checkNested, UastNode.Kind.FUNCTION_LITERAL, UastNode.Kind.CLASS);
  }

  public void checkNested(UastNode nestedSwitchNode) {
    if (!reported.contains(nestedSwitchNode)) {
      UastNode switchKeyword = SwitchLike.from(nestedSwitchNode).switchKeyword();
      reportIssue(switchKeyword, "Refactor the code to eliminate this nested \"switch\".");
      reported.add(nestedSwitchNode);
    }
  }

}
