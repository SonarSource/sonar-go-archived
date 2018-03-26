package org.sonar.commonruleengine.checks.go;

import org.junit.jupiter.api.Test;
import org.sonar.commonruleengine.checks.SwitchDefaultLocationCheck;

import static org.sonar.commonruleengine.checks.TestUtils.checkRuleOnGo;

public class SwitchDefaultLocationCheckTest {
  @Test
  void test() throws Exception {
    checkRuleOnGo(new SwitchDefaultLocationCheck());
  }
}
