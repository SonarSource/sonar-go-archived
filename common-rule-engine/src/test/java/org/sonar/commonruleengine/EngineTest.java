package org.sonar.commonruleengine;

import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.commonruleengine.checks.Check;
import org.sonar.uast.Uast;
import org.sonar.uast.UastNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EngineTest {

  private UastNode uast;

  @BeforeEach
  void setUp() throws Exception {
    uast = Uast.from(new InputStreamReader(getClass().getResourceAsStream("/uast.json")));
  }

  @Test
  void visit_should_visit_all_nodes() throws Exception {
    NodeCounter nodeCounter = new NodeCounter();
    Engine engine = new Engine(Collections.singletonList(nodeCounter));
    InputFile inputFile = TestInputFileBuilder.create(".", "foo.go").setType(InputFile.Type.MAIN).build();
    List<Issue> issues = engine.scan(uast, inputFile).issues;
    assertEquals(4, issues.size());
    assertTrue(issues.stream().map(Issue::getCheck).allMatch(rule -> rule == nodeCounter));
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
