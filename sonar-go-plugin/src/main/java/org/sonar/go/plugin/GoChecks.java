package org.sonar.go.plugin;

import java.util.Arrays;
import java.util.List;
import org.sonar.commonruleengine.checks.BinaryOperatorIdenticalExpressionsCheck;
import org.sonar.commonruleengine.checks.FunctionCognitiveComplexityCheck;
import org.sonar.commonruleengine.checks.NestedSwitchCheck;
import org.sonar.commonruleengine.checks.NoHardcodedCredentialsCheck;
import org.sonar.commonruleengine.checks.NoIdenticalConditionsCheck;
import org.sonar.commonruleengine.checks.NoIdenticalFunctionsCheck;
import org.sonar.commonruleengine.checks.NoSelfAssignmentCheck;
import org.sonar.commonruleengine.checks.RedundantBooleanLiteralCheck;
import org.sonar.commonruleengine.checks.SwitchDefaultLocationCheck;
import org.sonar.commonruleengine.checks.SwitchWithoutDefaultCheck;
import org.sonar.commonruleengine.checks.TooManyParametersCheck;
import org.sonar.commonruleengine.checks.UnconditionalJumpStatementCheck;

public class GoChecks {

  private GoChecks() {
    // do not instantiate
  }

  public static List<Class> getChecks() {
    return Arrays.asList(
      BinaryOperatorIdenticalExpressionsCheck.class,
      FunctionCognitiveComplexityCheck.class,
      NestedSwitchCheck.class,
      NoIdenticalConditionsCheck.class,
      NoIdenticalFunctionsCheck.class,
      NoHardcodedCredentialsCheck.class,
      NoSelfAssignmentCheck.class,
      RedundantBooleanLiteralCheck.class,
      SwitchDefaultLocationCheck.class,
      SwitchWithoutDefaultCheck.class,
      UnconditionalJumpStatementCheck.class,
      TooManyParametersCheck.class
    );
  }

}
