package org.sonar.commonruleengine.checks.go;

import org.junit.jupiter.api.Test;
import org.sonar.commonruleengine.checks.NoIdenticalConditionsCheck;

import static org.sonar.commonruleengine.checks.TestUtils.checkRuleOnGo;

class NoIdenticalConditionsCheckTest {

  @Test
  void test() throws Exception {
    checkRuleOnGo(new NoIdenticalConditionsCheck());
  }

  @Test
  void test_switch() throws Exception {
    checkRuleOnGo(new NoIdenticalConditionsCheck(), "NoIdenticalConditionsCheckSwitch.go");
  }
}
