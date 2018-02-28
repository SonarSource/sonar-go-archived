package org.sonar.uast.generator.java;

import java.util.ArrayList;
import java.util.List;

public class JavaTestUtils {
  public static List<Integer> expectedIssues(String source) {
    String[] lines = source.split("\n");
    List<Integer> expectedIssues = new ArrayList<>();
    for (int i = 0; i < lines.length; i++) {
      if (lines[i].contains("Noncompliant")) {
        expectedIssues.add(i + 1);
      }
    }
    return expectedIssues;
  }
}
