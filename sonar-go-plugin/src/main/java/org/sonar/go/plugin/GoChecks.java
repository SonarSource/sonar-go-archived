package org.sonar.go.plugin;

import java.util.Arrays;
import java.util.List;
import org.sonar.commonruleengine.checks.NoHardcodedCredentialsCheck;

public class GoChecks {

  private GoChecks() {
    // do not instantiate
  }

  public static List<Class> getChecks() {
    return Arrays.asList(
      NoHardcodedCredentialsCheck.class
    );
  }

}
