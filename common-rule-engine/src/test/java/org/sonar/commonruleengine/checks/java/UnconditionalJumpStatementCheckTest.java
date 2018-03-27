package org.sonar.commonruleengine.checks.java;

import org.junit.jupiter.api.Test;
import org.sonar.commonruleengine.checks.TooManyParametersCheck;
import org.sonar.commonruleengine.checks.UnconditionalJumpStatementCheck;

import static org.sonar.commonruleengine.checks.TestUtils.checkRuleOnJava;

class UnconditionalJumpStatementCheckTest {

  @Test
  void test() throws Exception {
    checkRuleOnJava(new UnconditionalJumpStatementCheck());
  }
}
