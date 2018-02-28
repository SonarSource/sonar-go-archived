package org.sonar.commonruleengine;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.sonar.commonruleengine.checks.Check;
import org.sonar.commonruleengine.checks.NoIdenticalFunctionsCheck;
import org.sonar.commonruleengine.checks.NoSelfAssignmentCheck;
import org.sonar.uast.Uast;
import org.sonar.uast.UastNode;

public class Engine {

  private static final List<Check> ALL_RULES = Arrays.asList(
    new NoIdenticalFunctionsCheck(),
    new NoSelfAssignmentCheck()
  );
  private final List<Check> rules;

  public static void main(String[] args) {
    try {
      BufferedReader inputReader = Files.newBufferedReader(Paths.get(args[0]));
      UastNode uast = Uast.from(inputReader);
      Engine engine = new Engine(ALL_RULES);
      List<Issue> issues = engine.scan(uast);
      issues.forEach(System.out::println);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public Engine(List<Check> rules) {
    this.rules = rules;
  }

  public List<Issue> scan(UastNode uast) {
    EngineContext engineContext = new EngineContext();
    rules.forEach(rule -> rule.setContext(engineContext));
    visit(uast);
    return engineContext.getIssues();
  }

  private void visit(UastNode uast) {
    for (Check rule : rules) {
      rule.visitNode(uast);
      for (UastNode child : uast.children) {
        visit(child);
      }
    }
  }
}
