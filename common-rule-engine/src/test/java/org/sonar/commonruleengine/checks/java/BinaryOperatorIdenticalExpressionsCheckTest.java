package org.sonar.commonruleengine.checks.java;

import org.junit.jupiter.api.Test;
import org.sonar.commonruleengine.checks.BinaryOperatorIdenticalExpressionsCheck;

import static org.sonar.commonruleengine.checks.TestUtils.checkRuleOnJava;

public class BinaryOperatorIdenticalExpressionsCheckTest {

  @Test
  void test() throws Exception {
    checkRuleOnJava(new BinaryOperatorIdenticalExpressionsCheck());
  }
}
