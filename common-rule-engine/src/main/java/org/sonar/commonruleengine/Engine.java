package org.sonar.commonruleengine;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.sonar.commonruleengine.rules.CommonRule;
import org.sonar.commonruleengine.rules.NoIdenticalMethodsRule;
import org.sonar.uast.Uast;
import org.sonar.uast.UastNode;

public class Engine {

  private static final List<CommonRule> ALL_RULES = Arrays.asList(new NoIdenticalMethodsRule());
  private final List<CommonRule> rules;
  private final EngineContext engineContext;

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

  public Engine(List<CommonRule> rules) {
    this.rules = rules;
    this.engineContext = new EngineContext();
    for (CommonRule rule : rules) {
      rule.setContext(engineContext);
    }
  }

  public List<Issue> scan(UastNode uast) {
    visit(uast);
    return engineContext.getIssues();
  }

  private void visit(UastNode uast) {
    for (CommonRule rule : rules) {
      rule.visitNode(uast);
      for (UastNode child : uast.children) {
        visit(child);
      }
    }
  }
}
