package org.sonar.commonruleengine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.commonruleengine.checks.Check;
import org.sonar.uast.Uast;
import org.sonar.uast.UastNode;

import static java.nio.charset.StandardCharsets.UTF_8;
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

  @Test
  void report() throws IOException {
    Path testFolder = Paths.get("src","test","resources",
      "org","sonar","commonruleengine","checks","go","NoSelfAssignmentCheck");
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Engine.report(new PrintStream(out,true, UTF_8.name()), new Engine.Parameters(new String[]{
        "--metrics",
        testFolder.resolve("NoSelfAssignmentCheck.go.uast.json").toString(),
        testFolder.resolve("NoSelfAssignmentCheck.go").toString()
      }));
    String actual = new String(out.toByteArray(), UTF_8).replace('\\', '/');
    String expected = new String(Files.readAllBytes(testFolder.resolve("NoSelfAssignmentCheck.report.txt")), UTF_8);
    assertEquals(expected, actual);
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
