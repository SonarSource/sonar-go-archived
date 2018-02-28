package org.sonar.uast.generator.java;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.sonar.commonruleengine.Engine;
import org.sonar.commonruleengine.Issue;
import org.sonar.commonruleengine.checks.Check;
import org.sonar.uast.UastNode;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JavaTestUtils {

  static List<Integer> expectedIssues(String source) {
    String[] lines = source.split("\n");
    List<Integer> expectedIssues = new ArrayList<>();
    for (int i = 0; i < lines.length; i++) {
      if (lines[i].contains("Noncompliant")) {
        expectedIssues.add(i + 1);
      }
    }
    return expectedIssues;
  }

  static void checkRule(Check check, String filename) throws IOException {
    UastWithIssues uastWithIssues = new UastWithIssues(filename);
    Engine engine = new Engine(Collections.singletonList(check));
    List<Issue> issues = engine.scan(uastWithIssues.uast);
    List<Integer> actualLines = issues.stream().map(Issue::getLine).collect(Collectors.toList());
    assertEquals(uastWithIssues.expectedIssues, actualLines);
  }

  static class UastWithIssues {
    UastNode uast;
    List<Integer> expectedIssues;

    UastWithIssues(String filename) throws IOException {
      String source = new String(Files.readAllBytes(Paths.get(filename)), StandardCharsets.UTF_8);
      Generator generator = new Generator(source);
      uast = generator.uast();
      expectedIssues = JavaTestUtils.expectedIssues(source);
    }
  }
}

