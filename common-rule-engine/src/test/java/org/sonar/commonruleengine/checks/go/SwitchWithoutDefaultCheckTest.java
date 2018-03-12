package org.sonar.commonruleengine.checks.go;

import org.junit.jupiter.api.Test;
import org.sonar.commonruleengine.checks.SwitchWithoutDefaultCheck;

import static org.sonar.commonruleengine.checks.TestUtils.checkRuleOnGo;

public class SwitchWithoutDefaultCheckTest {
  @Test
  void test() throws Exception {
    checkRuleOnGo(new SwitchWithoutDefaultCheck());
  }
}
