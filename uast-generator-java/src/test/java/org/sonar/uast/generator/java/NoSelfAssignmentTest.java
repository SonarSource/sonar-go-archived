package org.sonar.uast.generator.java;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.sonar.commonruleengine.Engine;
import org.sonar.commonruleengine.Issue;
import org.sonar.commonruleengine.checks.NoSelfAssignmentCheck;
import org.sonar.uast.UastNode;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NoSelfAssignmentTest {

  @Test
  void test() throws Exception {
    String source = Generator.fileContent("src/test/files/SelfAssignementCheck.java");
    Generator generator = new Generator(source);
    UastNode uast = generator.uast();
    Engine engine = new Engine(Collections.singletonList(new NoSelfAssignmentCheck()));
    List<Issue> issues = engine.scan(uast);
    List<Integer> expectedIssues = JavaTestUtils.expectedIssues(source);
    List<Integer> actualLines = issues.stream().map(Issue::getLine).collect(Collectors.toList());
    assertEquals(expectedIssues, actualLines);
  }

}
