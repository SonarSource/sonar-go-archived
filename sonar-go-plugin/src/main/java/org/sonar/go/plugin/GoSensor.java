package org.sonar.go.plugin;

import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.measure.Metric;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.rule.RuleKey;
import org.sonar.commonruleengine.Engine;
import org.sonar.commonruleengine.Issue;
import org.sonar.commonruleengine.Metrics;
import org.sonar.commonruleengine.checks.Check;
import org.sonar.uast.UastNode;

public class GoSensor implements Sensor {

  private final Checks<Check> checks;
  private final FileLinesContextFactory fileLinesContextFactory;

  public GoSensor(CheckFactory checkFactory, FileLinesContextFactory fileLinesContextFactory) {
    checks = checkFactory.<Check>create(GoRulesDefinition.REPOSITORY_KEY)
      .addAnnotatedChecks((Iterable) GoChecks.getChecks());
    this.fileLinesContextFactory = fileLinesContextFactory;
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor.onlyOnLanguage(GoLanguage.KEY)
      .name("SonarGo");
  }

  @Override
  public void execute(SensorContext context) {
    Engine ruleEngine = new Engine(checks.all());
    String lastAnalyzedFile = "no file analyzed";
    try {
      UastGeneratorWrapper uastGenerator = new UastGeneratorWrapper(context.fileSystem().workDir());
      for (InputFile inputFile : getInputFiles(context)) {
        lastAnalyzedFile = inputFile.toString();
        UastNode uast = uastGenerator.createUast(inputFile);
        Engine.ScanResult scanResult = ruleEngine.scan(uast);
        scanResult.issues.forEach(issue -> reportIssue(issue, context, inputFile));
        saveMetrics(scanResult.metrics, context, inputFile);
        saveHighlighting(uast, context, inputFile);
      }
    } catch (IOException | RuntimeException e) {
      throw new GoPluginException("Error during analysis. Last analyzed file: \"" + lastAnalyzedFile + "\"", e);
    }
  }

  private void reportIssue(Issue issue, SensorContext context, InputFile inputFile) {
    // TODO improve common rule engine to handle this out of the box
    NewIssue newIssue = context.newIssue();
    TextRange textRange = inputFile.selectLine(issue.getLine());
    RuleKey ruleKey = checks.ruleKey(issue.getRule());
    Objects.requireNonNull(ruleKey, "Rule key not found for " + issue.getRule().getClass());
    newIssue
      .at(newIssue.newLocation().on(inputFile).at(textRange).message(issue.getMessage()))
      .forRule(ruleKey)
      .save();
  }

  private void saveMetrics(Metrics metrics, SensorContext context, InputFile inputFile) {
    saveMetric(context, inputFile, CoreMetrics.NCLOC, metrics.linesOfCode.size());
    saveMetric(context, inputFile, CoreMetrics.COMMENT_LINES, metrics.commentLines.size());
    saveMetric(context, inputFile, CoreMetrics.CLASSES, metrics.numberOfClasses);
    saveMetric(context, inputFile, CoreMetrics.FUNCTIONS, metrics.numberOfFunctions);
    saveMetric(context, inputFile, CoreMetrics.STATEMENTS, metrics.numberOfStatements);

    FileLinesContext fileLinesContext = fileLinesContextFactory.createFor(inputFile);
    metrics.linesOfCode.forEach(line -> fileLinesContext.setIntValue(CoreMetrics.NCLOC_DATA_KEY, line, 1));
    metrics.commentLines.forEach(line -> fileLinesContext.setIntValue(CoreMetrics.COMMENT_LINES_DATA_KEY, line, 1));
    fileLinesContext.save();
  }

  private static void saveHighlighting(UastNode uast, SensorContext context, InputFile inputFile) {
    HighlightingVisitor highlighting = new HighlightingVisitor(context, inputFile);
    highlighting.scan(uast);
    highlighting.save();
  }

  private static <T extends Serializable> void saveMetric(SensorContext context, InputFile inputFile, Metric<T> metric, T value) {
    context.<T>newMeasure()
      .on(inputFile)
      .forMetric(metric)
      .withValue(value)
      .save();
  }

  private static Iterable<InputFile> getInputFiles(SensorContext context) {
    FileSystem fs = context.fileSystem();
    return fs.inputFiles(fs.predicates().hasLanguage(GoLanguage.KEY));
  }

  static class GoPluginException extends RuntimeException {

    public GoPluginException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
