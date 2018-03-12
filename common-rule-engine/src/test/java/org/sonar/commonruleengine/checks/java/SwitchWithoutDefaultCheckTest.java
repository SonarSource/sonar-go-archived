package org.sonar.commonruleengine.checks.java;

import org.junit.jupiter.api.Test;
import org.sonar.commonruleengine.checks.SwitchWithoutDefaultCheck;

import static org.sonar.commonruleengine.checks.TestUtils.checkRuleOnJava;

public class SwitchWithoutDefaultCheckTest {
  @Test
  void test() throws Exception {
    checkRuleOnJava(new SwitchWithoutDefaultCheck());
  }
}
