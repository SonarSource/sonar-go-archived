package org.sonar.commonruleengine.checks.java;

import org.junit.jupiter.api.Test;
import org.sonar.commonruleengine.checks.NoIdenticalFunctionsCheck;

import static org.sonar.commonruleengine.checks.TestUtils.checkRuleOnJava;

class NoIdenticalFunctionsCheckTest {

  @Test
  void test() throws Exception {
    checkRuleOnJava(new NoIdenticalFunctionsCheck());
  }

}
