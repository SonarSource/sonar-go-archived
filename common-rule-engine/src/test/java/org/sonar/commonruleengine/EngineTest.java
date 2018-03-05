package org.sonar.commonruleengine;

import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.commonruleengine.checks.Check;
import org.sonar.uast.Uast;
import org.sonar.uast.UastNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EngineTest {

  private UastNode uast;

  @BeforeEach
  void setUp() {
    uast = Uast.from(new InputStreamReader(getClass().getResourceAsStream("/uast.json")));
  }

  @Test
  void visit_should_visit_all_nodes() {
    NodeCounter nodeCounter = new NodeCounter();
    Engine engine = new Engine(Collections.singletonList(nodeCounter));
    List<Issue> issues = engine.scan(uast).issues;
    assertEquals(4, issues.size());
    assertTrue(issues.stream().map(Issue::getRule).allMatch(rule -> rule == nodeCounter));
  }

  static class NodeCounter extends Check {
    int count;

    NodeCounter() {
      super(UastNode.Kind.values());
    }

    @Override
    public void visitNode(UastNode node) {
      count++;
      reportIssue(node, String.valueOf(count));
    }
  }
}
