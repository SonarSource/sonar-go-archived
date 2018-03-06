package org.sonar.commonruleengine.checks.go;

import org.junit.jupiter.api.Test;
import org.sonar.commonruleengine.checks.NoHardcodedCredentialsCheck;
import org.sonar.commonruleengine.checks.TestUtils;

class NoHardcodedCredentialsCheckTest {

  @Test
  void test() throws Exception {
    TestUtils.checkRuleOnGo(new NoHardcodedCredentialsCheck());
  }
}
