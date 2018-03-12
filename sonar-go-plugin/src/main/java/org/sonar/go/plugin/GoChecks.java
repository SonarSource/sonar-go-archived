package org.sonar.go.plugin;

import java.util.Arrays;
import java.util.List;
import org.sonar.commonruleengine.checks.BinaryOperatorIdenticalExpressionsCheck;
import org.sonar.commonruleengine.checks.NoHardcodedCredentialsCheck;
import org.sonar.commonruleengine.checks.NoIdenticalConditionsCheck;
import org.sonar.commonruleengine.checks.NoIdenticalFunctionsCheck;
import org.sonar.commonruleengine.checks.NoSelfAssignmentCheck;
import org.sonar.commonruleengine.checks.SwitchWithoutDefaultCheck;
import org.sonar.commonruleengine.checks.TooManyParametersCheck;

public class GoChecks {

  private GoChecks() {
    // do not instantiate
  }

  public static List<Class> getChecks() {
    return Arrays.asList(
      BinaryOperatorIdenticalExpressionsCheck.class,
      NoIdenticalConditionsCheck.class,
      NoIdenticalFunctionsCheck.class,
      NoHardcodedCredentialsCheck.class,
      NoSelfAssignmentCheck.class,
      SwitchWithoutDefaultCheck.class,
      TooManyParametersCheck.class
    );
  }

}
