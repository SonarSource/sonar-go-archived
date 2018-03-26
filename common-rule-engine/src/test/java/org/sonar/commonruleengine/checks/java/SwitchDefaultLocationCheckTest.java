package org.sonar.commonruleengine.checks.java;

import org.junit.jupiter.api.Test;
import org.sonar.commonruleengine.checks.SwitchDefaultLocationCheck;

import static org.sonar.commonruleengine.checks.TestUtils.checkRuleOnJava;

public class SwitchDefaultLocationCheckTest {
  @Test
  void test() throws Exception {
    checkRuleOnJava(new SwitchDefaultLocationCheck());
  }
}
