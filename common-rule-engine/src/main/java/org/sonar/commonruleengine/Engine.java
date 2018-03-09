package org.sonar.commonruleengine;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import org.sonar.commonruleengine.checks.Check;
import org.sonar.commonruleengine.checks.CheckList;
import org.sonar.uast.Uast;
import org.sonar.uast.UastNode;

public class Engine {

  private final EngineContext engineContext;
  private final MetricsVisitor metricsVisitor;

  public static void main(String[] args) throws IOException {
    report(System.out, new Parameters(args));
  }

  static void report(PrintStream out, Parameters params) throws IOException {
    UastNode uast = Uast.from(Files.newBufferedReader(params.uastPath));
    Engine engine = new Engine();
    ScanResult scanResult = engine.scan(uast);
    IssueReport report = new IssueReport(scanResult, params.goSourcePath, params.color, params.metrics);
    report.writeTo(out);
  }

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

  public Engine(List<Check> rules) {
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

  static class Parameters {
    boolean color = false;
    boolean metrics = false;
    Path uastPath = null;
    Path goSourcePath = null;

    public Parameters(String[] args) {
      for (String arg : args) {
        switch (arg) {
          case "--color":
            color = true;
            break;
          case "--metrics":
            metrics = true;
            break;
          default:
            if (uastPath == null) {
              uastPath = Paths.get(arg);
            } else if (goSourcePath == null) {
              goSourcePath = Paths.get(arg);
            } else {
              fatal("Too many file names provided.");
            }
            break;
        }
      }
      if (uastPath == null) {
        fatal("Missing parameters. Syntax: [--color] [--metrics] uast.json source.go");
      }
      if (goSourcePath == null) {
        fatal("Missing go source file, you need to provide uast path and go file path.");
      }
      if (!Files.exists(uastPath)) {
        fatal("File not found: " + uastPath);
      }
      if (!Files.exists(goSourcePath)) {
        fatal("File not found: " + goSourcePath);
      }
    }
  }

  private static void fatal(String error) {
    System.err.println("[ERROR] " + error);
    System.exit(1);
  }

}
