package org.sonar.commonruleengine.checks;

import java.util.HashSet;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.commonruleengine.EngineContext;
import org.sonar.uast.UastNode;

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
  public void initialize(EngineContext context) {
    reported.clear();
    super.initialize(context);
  }

  @Override
  public void visitNode(UastNode node) {
    reported.add(node);
    node.getDescendants(UastNode.Kind.SWITCH, this::checkNested, UastNode.Kind.FUNCTION_LITERAL, UastNode.Kind.CLASS);
  }

  public void checkNested(UastNode nestedSwitch) {
    if (!reported.contains(nestedSwitch)) {
      reportIssue(nestedSwitch, "Refactor the code to eliminate this nested \"switch\".");
      reported.add(nestedSwitch);
    }
  }

}
