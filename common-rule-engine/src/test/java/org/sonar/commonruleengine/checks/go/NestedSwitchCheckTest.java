package org.sonar.commonruleengine.checks.go;

import org.junit.jupiter.api.Test;
import org.sonar.commonruleengine.checks.NestedSwitchCheck;
import org.sonar.commonruleengine.checks.TestUtils;

class NestedSwitchCheckTest {

  @Test
  void test() throws Exception {
    TestUtils.checkRuleOnGo(new NestedSwitchCheck());
  }
}

