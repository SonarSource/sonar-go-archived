package org.sonar.commonruleengine;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import org.sonar.commonruleengine.checks.Check;
import org.sonar.commonruleengine.checks.CheckList;
import org.sonar.uast.Uast;
import org.sonar.uast.UastNode;

public class Engine {

  public static final List<Check> ALL_CHECKS;

  static {
    ALL_CHECKS = CheckList.getChecks().stream().map(c -> {
      try {
        return c.newInstance();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }).collect(Collectors.toList());
  }

  private final EngineContext engineContext;

  public static void main(String[] args) {
    try {
      BufferedReader inputReader = Files.newBufferedReader(Paths.get(args[0]));
      UastNode uast = Uast.from(inputReader);
      Engine engine = new Engine(ALL_CHECKS);
      List<Issue> issues = engine.scan(uast);
      issues.forEach(System.out::println);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public Engine(List<Check> rules) {
    engineContext = new EngineContext();
    rules.forEach(rule -> rule.initialize(engineContext));
  }

  public List<Issue> scan(UastNode uast) {
    engineContext.enterFile();
    visit(uast);
    return engineContext.getIssues();
  }

  private void visit(UastNode uast) {
    for (UastNode.Kind kind : uast.kinds) {
      for (Check rule : engineContext.registeredChecks(kind)) {
        rule.visitNode(uast);
      }
    }
    for (UastNode child : uast.children) {
      visit(child);
    }
  }
}
