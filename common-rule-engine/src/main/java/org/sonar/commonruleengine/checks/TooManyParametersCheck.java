package org.sonar.commonruleengine.checks;

import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.uast.UastNode;
import org.sonar.uast.helpers.FunctionLike;

/**
 * https://jira.sonarsource.com/browse/RSPEC-107
 */
@Rule(key = "S107")
public class TooManyParametersCheck extends Check {

  private static final int DEFAULT_MAXIMUM = 7;

  @RuleProperty(
    key = "max",
    description = "Maximum authorized number of parameters",
    defaultValue = "" + DEFAULT_MAXIMUM)
  public int maximum = DEFAULT_MAXIMUM;

  public TooManyParametersCheck() {
    super(UastNode.Kind.FUNCTION);
  }

  @Override
  public void visitNode(UastNode node) {
    FunctionLike function = FunctionLike.from(node);
    if (function != null && function.parameters().size() > maximum) {
      reportIssue(function.name(),
        String.format("Function has %d parameters, which is more than %d authorized.", function.parameters().size(), maximum));
    }
  }
}
