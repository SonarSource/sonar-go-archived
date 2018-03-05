package org.sonar.commonruleengine.checks.go;

import org.junit.jupiter.api.Test;
import org.sonar.commonruleengine.checks.NoIdenticalFunctionsCheck;
import org.sonar.commonruleengine.checks.NoSelfAssignmentCheck;

import static org.sonar.commonruleengine.checks.TestUtils.checkRuleOnGo;

class NoIdenticalFunctionsCheckTest {

  @Test
  void test() throws Exception {
    checkRuleOnGo(new NoIdenticalFunctionsCheck());
  }

}
