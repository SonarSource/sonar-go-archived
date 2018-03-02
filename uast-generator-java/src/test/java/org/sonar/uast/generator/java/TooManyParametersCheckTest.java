package org.sonar.uast.generator.java;

import org.junit.jupiter.api.Test;
import org.sonar.commonruleengine.checks.TooManyParametersCheck;

import static org.sonar.uast.generator.java.JavaTestUtils.checkRule;

class TooManyParametersCheckTest {

  @Test
  void test() throws Exception {
    checkRule(new TooManyParametersCheck(), "src/test/files/TooManyParameters.java");
  }

  @Test
  void test_custom_threshold() throws Exception {
    TooManyParametersCheck check = new TooManyParametersCheck();
    check.maximum = 3;
    checkRule(check, "src/test/files/TooManyParametersMax3.java");
  }
}
