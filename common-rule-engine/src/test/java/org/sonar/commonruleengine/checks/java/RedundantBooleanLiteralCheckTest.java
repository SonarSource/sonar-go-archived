package org.sonar.commonruleengine.checks.java;

import org.junit.jupiter.api.Test;
import org.sonar.commonruleengine.checks.RedundantBooleanLiteralCheck;

import static org.sonar.commonruleengine.checks.TestUtils.checkRuleOnJava;

class RedundantBooleanLiteralCheckTest {

  @Test
  void test() throws Exception {
    checkRuleOnJava(new RedundantBooleanLiteralCheck());
  }

}
