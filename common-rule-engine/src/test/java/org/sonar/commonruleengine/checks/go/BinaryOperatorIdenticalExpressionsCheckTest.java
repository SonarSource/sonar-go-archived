package org.sonar.commonruleengine.checks.go;

import org.junit.jupiter.api.Test;
import org.sonar.commonruleengine.checks.BinaryOperatorIdenticalExpressionsCheck;
import org.sonar.commonruleengine.checks.TestUtils;

class BinaryOperatorIdenticalExpressionsCheckTest {

  @Test
  void test() throws Exception {
    TestUtils.checkRuleOnGo(new BinaryOperatorIdenticalExpressionsCheck());
  }
}
