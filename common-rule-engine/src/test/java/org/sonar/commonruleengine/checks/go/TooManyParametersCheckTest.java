package org.sonar.commonruleengine.checks.go;

import org.junit.jupiter.api.Test;
import org.sonar.commonruleengine.checks.TooManyParametersCheck;

import static org.sonar.commonruleengine.checks.TestUtils.checkRuleOnGo;

class TooManyParametersCheckTest {

  @Test
  void test() throws Exception {
    checkRuleOnGo(new TooManyParametersCheck());
  }

  @Test
  void test_custom_threshold() throws Exception {
    TooManyParametersCheck check = new TooManyParametersCheck();
    check.maximum = 3;
    checkRuleOnGo(check, "TooManyParametersCheckMax3.go");
  }
}
