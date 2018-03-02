package org.sonar.commonruleengine.checks.go;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.commonruleengine.Engine;
import org.sonar.commonruleengine.Issue;
import org.sonar.commonruleengine.UastUtils;
import org.sonar.commonruleengine.checks.NoSelfAssignmentCheck;
import org.sonar.uast.UastNode;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NoSelfAssignmentCheckTest {

  static UastNode fromClasspath(String resource) {
    return UastUtils.fromClasspath(NoSelfAssignmentCheckTest.class, resource);
  }

  @Test
  void single_variable_self_assignment_raises_issue() {
    UastNode uast = fromClasspath("noSelfAssignmentSingle.go.uast.json");
    Engine engine = createEngine();
    List<Issue> issues = engine.scan(uast);
    assertEquals(1, issues.size());
  }

  @Test
  void multi_variable_self_assignment_raises_issue() {
    UastNode uast = fromClasspath("noSelfAssignmentMulti.go.uast.json");
    Engine engine = createEngine();
    List<Issue> issues = engine.scan(uast);
    assertEquals(1, issues.size());
  }

  @Test
  void non_self_single_assignment_does_not_raise_issue() {
    Engine engine = createEngine();
    UastNode uast = fromClasspath("noSelfAssignmentSingleNoIssue.go.uast.json");
    List<Issue> issues = engine.scan(uast);
    assertEquals(0, issues.size());
  }

  @Test
  void non_self_multi_assignment_does_not_raise_issue() {
    Engine engine = createEngine();
    UastNode uast = fromClasspath("noSelfAssignmentMultiNoIssue.go.uast.json");
    List<Issue> issues = engine.scan(uast);
    assertEquals(0, issues.size());
  }

  @Test
  void partial_self_multi_assignment_does_not_raise_issue() {
    Engine engine = createEngine();
    UastNode uast = fromClasspath("noSelfAssignmentPartialNoIssue.go.uast.json");
    List<Issue> issues = engine.scan(uast);
    assertEquals(0, issues.size());
  }

  private Engine createEngine() {
    return new Engine(Collections.singletonList(new NoSelfAssignmentCheck()));
  }
}
