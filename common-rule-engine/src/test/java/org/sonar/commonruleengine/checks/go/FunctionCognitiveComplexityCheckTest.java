package org.sonar.commonruleengine.checks.go;

import org.junit.jupiter.api.Test;
import org.sonar.commonruleengine.checks.FunctionCognitiveComplexityCheck;
import org.sonar.commonruleengine.checks.TestUtils;

public class FunctionCognitiveComplexityCheckTest {

  @Test
  void test() throws Exception {
    FunctionCognitiveComplexityCheck check = new FunctionCognitiveComplexityCheck();
    check.setMaxComplexity(0);
    TestUtils.checkRuleOnGo(check);
  }

}
