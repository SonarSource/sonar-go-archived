package org.sonar.commonruleengine;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.sonar.uast.UastNode;

public class IssueReport {

  private static final String ANSI_RESET = "\u001B[0m";
  private static final String ANSI_RED = "\u001B[31m";
  private static final String ANSI_CYAN = "\u001B[36m";

  private Engine.ScanResult scanResult;

  private final Path goSourcePath;
  private final boolean metrics;
  private List<String> goSourceLines;
  private final String colorRed;
  private final String colorCyan;
  private final String colorReset;

  public IssueReport(Engine.ScanResult scanResult, Path goSourcePath, boolean color, boolean metrics) {
    this.scanResult = scanResult;
    this.goSourcePath = goSourcePath;
    this.goSourceLines = null;
    colorRed = color ? ANSI_RED : "";
    colorCyan = color ? ANSI_CYAN : "";
    colorReset = color ? ANSI_RESET : "";
    this.metrics = metrics;
  }

  public void writeTo(PrintStream out) throws IOException {
    if (!scanResult.issues.isEmpty()) {
      for (Issue issue : scanResult.issues) {
        String ruleName = issue.getRule().getClass().getSimpleName();
        UastNode node = issue.getNode().firstToken();
        if (node != null && node.token != null) {
          appendIssue(out, node.token, ruleName, issue.getMessage());
        }
      }
    }
    if (metrics) {
      out.println(            "___ Metrics of " + goSourcePath.toString() + " ___");
      out.println(colorCyan + "Number Of Classes           : " + scanResult.metrics.numberOfClasses + colorReset);
      out.println(colorCyan + "Number Of Functions         : " + scanResult.metrics.numberOfFunctions + colorReset);
      out.println(colorCyan + "Number Of Statements        : " + scanResult.metrics.numberOfStatements + colorReset);
      out.println(colorCyan + "Number Of Lines Of Code     : " + scanResult.metrics.linesOfCode.size() + colorReset);
      out.println(colorCyan + "Number Of Lines Of Comments : " + scanResult.metrics.commentLines.size() + colorReset);
      out.println();
    }
  }

  private void appendIssue(PrintStream out, UastNode.Token token, String ruleName, String message) throws IOException {
    String location = goSourcePath.toString() + ":" + token.line + ":" + token.column;
    out.println(colorRed + ruleName + " in " + location + colorReset);
    appendSourceCodeLines(out, token.line - 3, token.line);
    for (int i = 1; i < token.column; i++) {
      out.print(" ");
    }
    out.println(colorCyan + "^ Noncompliant: " + message + colorReset);
    out.println();
  }

  private void appendSourceCodeLines(PrintStream out, int from, int to) throws IOException {
    if (goSourceLines == null) {
      goSourceLines = Files.readAllLines(goSourcePath, StandardCharsets.UTF_8);
    }
    for (int i = Math.max(1, from); i <= to && i <= goSourceLines.size(); i++) {
      out.println(goSourceLines.get(i - 1).replace('\t', ' '));
    }
  }

}
