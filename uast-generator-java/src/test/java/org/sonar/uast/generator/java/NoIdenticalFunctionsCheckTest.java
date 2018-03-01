package org.sonar.uast.generator.java;

import org.junit.jupiter.api.Test;
import org.sonar.commonruleengine.checks.NoIdenticalFunctionsCheck;

import static org.sonar.uast.generator.java.JavaTestUtils.checkRule;

class NoIdenticalFunctionsCheckTest {

  @Test
  void test() throws Exception {
    checkRule(new NoIdenticalFunctionsCheck(), "src/test/files/MethodIdenticalImplementationsCheck.java");
  }

}
