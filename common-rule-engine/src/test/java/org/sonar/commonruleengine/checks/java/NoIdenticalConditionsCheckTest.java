package org.sonar.commonruleengine.checks.java;

import org.junit.jupiter.api.Test;
import org.sonar.commonruleengine.checks.NoIdenticalConditionsCheck;

import static org.sonar.commonruleengine.checks.TestUtils.checkRuleOnJava;

class NoIdenticalConditionsCheckTest {

  @Test
  void test() throws Exception {
    checkRuleOnJava(new NoIdenticalConditionsCheck());
  }
}
