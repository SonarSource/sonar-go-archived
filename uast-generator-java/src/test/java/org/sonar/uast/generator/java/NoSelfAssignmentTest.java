package org.sonar.uast.generator.java;

import org.junit.jupiter.api.Test;
import org.sonar.commonruleengine.checks.NoSelfAssignmentCheck;

import static org.sonar.uast.generator.java.JavaTestUtils.checkRule;

public class NoSelfAssignmentTest {

  @Test
  void test() throws Exception {
    checkRule(new NoSelfAssignmentCheck(), "src/test/files/SelfAssignementCheck.java");
  }

}
