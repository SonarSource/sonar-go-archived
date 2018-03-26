package org.sonar.commonruleengine.checks.go;

import org.junit.jupiter.api.Test;
import org.sonar.commonruleengine.checks.RedundantBooleanLiteralCheck;

import static org.sonar.commonruleengine.checks.TestUtils.checkRuleOnGo;

class RedundantBooleanLiteralCheckTest {

  @Test
  void test() throws Exception {
    checkRuleOnGo(new RedundantBooleanLiteralCheck());
  }

}
