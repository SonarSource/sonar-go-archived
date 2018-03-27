package org.sonar.commonruleengine.checks.go;

import org.junit.jupiter.api.Test;
import org.sonar.commonruleengine.checks.UnconditionalJumpStatementCheck;

import static org.sonar.commonruleengine.checks.TestUtils.checkRuleOnGo;

class UnconditionalJumpStatementCheckTest {

  @Test
  void test() throws Exception {
    checkRuleOnGo(new UnconditionalJumpStatementCheck());
  }
}
