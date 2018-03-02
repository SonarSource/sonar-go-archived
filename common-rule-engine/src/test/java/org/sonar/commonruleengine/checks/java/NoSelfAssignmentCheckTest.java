package org.sonar.commonruleengine.checks.java;

import org.junit.jupiter.api.Test;
import org.sonar.commonruleengine.checks.NoSelfAssignmentCheck;

import static org.sonar.commonruleengine.checks.TestUtils.checkRuleOnJava;

public class NoSelfAssignmentCheckTest {

  @Test
  void test() throws Exception {
    checkRuleOnJava(new NoSelfAssignmentCheck());
  }

}
