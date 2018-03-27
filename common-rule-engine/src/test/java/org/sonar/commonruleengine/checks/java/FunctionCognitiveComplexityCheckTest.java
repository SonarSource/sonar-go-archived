package org.sonar.commonruleengine.checks.java;

import org.junit.jupiter.api.Test;
import org.sonar.commonruleengine.checks.FunctionCognitiveComplexityCheck;
import org.sonar.commonruleengine.checks.TestUtils;

public class FunctionCognitiveComplexityCheckTest {

  @Test
  void test() throws Exception {
    FunctionCognitiveComplexityCheck check = new FunctionCognitiveComplexityCheck();
    check.setMaxComplexity(0);
    TestUtils.checkRuleOnJava(check);
  }

}
