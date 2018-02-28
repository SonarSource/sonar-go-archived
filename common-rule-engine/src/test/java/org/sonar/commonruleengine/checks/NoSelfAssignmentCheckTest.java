package org.sonar.commonruleengine.checks;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.commonruleengine.Engine;
import org.sonar.commonruleengine.Issue;
import org.sonar.uast.UastNode;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NoSelfAssignmentCheckTest {

  @Test
  void test() {
    UastNode uast = Utils.fromClasspath("noSelfAssignment.uast.json");
    Engine engine = new Engine(Collections.singletonList(new NoSelfAssignmentCheck()));
    List<Issue> issues = engine.scan(uast);
    assertEquals(1, issues.size());
  }

  @Test
  void test_no_issue() {
    Engine engine = new Engine(Collections.singletonList(new NoSelfAssignmentCheck()));
    UastNode uast = Utils.fromClasspath("noSelfAssignmentNoIssue.uast.json");
    List<Issue> issues = engine.scan(uast);
    assertEquals(0, issues.size());
  }

  @Test
  void test_no_issue2() {
    Engine engine = new Engine(Collections.singletonList(new NoSelfAssignmentCheck()));
    UastNode uast = Utils.fromClasspath("noSelfAssignmentNoIssue2.uast.json");
    List<Issue> issues = engine.scan(uast);
    assertEquals(0, issues.size());
  }
}
