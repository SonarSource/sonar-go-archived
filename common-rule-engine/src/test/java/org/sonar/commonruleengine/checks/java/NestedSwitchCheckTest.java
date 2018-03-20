package org.sonar.commonruleengine.checks.java;

import org.junit.jupiter.api.Test;
import org.sonar.commonruleengine.checks.NestedSwitchCheck;
import org.sonar.commonruleengine.checks.TestUtils;

class NestedSwitchCheckTest {

  @Test
  void test() throws Exception {
    TestUtils.checkRuleOnJava(new NestedSwitchCheck());
  }
}

