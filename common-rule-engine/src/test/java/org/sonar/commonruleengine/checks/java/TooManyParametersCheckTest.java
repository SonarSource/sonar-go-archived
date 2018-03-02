package org.sonar.commonruleengine.checks.java;

import org.junit.jupiter.api.Test;
import org.sonar.commonruleengine.checks.TooManyParametersCheck;

import static org.sonar.commonruleengine.checks.TestUtils.checkRuleOnJava;

class TooManyParametersCheckTest {

  @Test
  void test() throws Exception {
    checkRuleOnJava(new TooManyParametersCheck());
  }

  @Test
  void test_custom_threshold() throws Exception {
    TooManyParametersCheck check = new TooManyParametersCheck();
    check.maximum = 3;
    checkRuleOnJava(check, "TooManyParametersCheckMax3.java");
  }
}
