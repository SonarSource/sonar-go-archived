package org.sonar.commonruleengine.checks;

import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.commonruleengine.Engine;
import org.sonar.commonruleengine.Issue;
import org.sonar.uast.Uast;
import org.sonar.uast.UastNode;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NoSelfAssignmentCheckTest {

  static UastNode fromClasspath(String resource) {
    return Uast.from(new InputStreamReader(NoSelfAssignmentCheck.class.getResourceAsStream(resource)));
  }

  @Test
  void test() {
    UastNode uast = fromClasspath("noSelfAssignment.uast.json");
    Engine engine = new Engine(Collections.singletonList(new NoSelfAssignmentCheck()));
    List<Issue> issues = engine.scan(uast);
    assertEquals(1, issues.size());
  }

  @Test
  void test_no_issue() {
    Engine engine = new Engine(Collections.singletonList(new NoSelfAssignmentCheck()));
    UastNode uast = fromClasspath("noSelfAssignmentNoIssue.uast.json");
    List<Issue> issues = engine.scan(uast);
    assertEquals(0, issues.size());
  }

  @Test
  void test_no_issue2() {
    Engine engine = new Engine(Collections.singletonList(new NoSelfAssignmentCheck()));
    UastNode uast = fromClasspath("noSelfAssignmentNoIssue2.uast.json");
    List<Issue> issues = engine.scan(uast);
    assertEquals(0, issues.size());
  }
}
