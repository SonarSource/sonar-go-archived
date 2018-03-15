package org.sonar.commonruleengine;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.sonar.commonruleengine.checks.Check;
import org.sonar.commonruleengine.checks.CheckList;
import org.sonar.uast.UastNode;

public class Engine {

  private final EngineContext engineContext;
  private final MetricsVisitor metricsVisitor;

  private static List<Check> initChecks() {
    return CheckList.getChecks().stream().map(c -> {
      try {
        return c.newInstance();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }).collect(Collectors.toList());
  }

  public Engine() {
    this(initChecks());
  }

  public Engine(Collection<Check> rules) {
    engineContext = new EngineContext();
    metricsVisitor = new MetricsVisitor();
    rules.forEach(rule -> rule.initialize(engineContext));
  }

  public ScanResult scan(UastNode uast) {
    metricsVisitor.enterFile();
    engineContext.enterFile();
    visit(uast);
    return new ScanResult(engineContext.getIssues(), metricsVisitor.getMetrics());
  }

  private void visit(UastNode uast) {
    metricsVisitor.visitNode(uast);
    for (UastNode.Kind kind : uast.kinds) {
      for (Check rule : engineContext.registeredChecks(kind)) {
        rule.visitNode(uast);
      }
    }
    for (UastNode child : uast.children) {
      visit(child);
    }
  }

  public static class ScanResult {
    public final List<Issue> issues;
    public final Metrics metrics;

    public ScanResult(List<Issue> issues, Metrics metrics) {
      this.issues = issues;
      this.metrics = metrics;
    }
  }
}
