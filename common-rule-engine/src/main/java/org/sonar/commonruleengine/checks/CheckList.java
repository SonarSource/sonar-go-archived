package org.sonar.commonruleengine.checks;

import java.util.Arrays;
import java.util.List;

public class CheckList {

  private CheckList() {
    // do not instantiate
  }

  public static List<Class<? extends Check>> getChecks() {
    return Arrays.asList(
      BinaryOperatorIdenticalExpressionsCheck.class,
      NestedSwitchCheck.class,
      NoIdenticalConditionsCheck.class,
      NoIdenticalFunctionsCheck.class,
      NoHardcodedCredentialsCheck.class,
      NoSelfAssignmentCheck.class,
      SwitchDefaultLocationCheck.class,
      SwitchWithoutDefaultCheck.class,
      TooManyParametersCheck.class
    );
  }

}
