package org.sonar.go.plugin;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
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
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.commonruleengine.Engine;
import org.sonar.commonruleengine.Issue;
import org.sonar.commonruleengine.Metrics;
import org.sonar.commonruleengine.checks.Check;
import org.sonar.uast.UastNode;

public class GoSensor implements Sensor {

  private static final Logger LOG = Loggers.get(GoSensor.class);

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
    UastGeneratorWrapper uastGenerator;
    try {
      uastGenerator = new UastGeneratorWrapper(context);
    } catch (Exception e) {
      throw new GoPluginException("Error initializing UAST generator", e);
    }
    List<InputFile> failedFiles = new ArrayList<>();
    for (InputFile inputFile : getInputFiles(context)) {
      try {
        UastNode uast = uastGenerator.createUast(inputFile.inputStream());
        Engine.ScanResult scanResult = ruleEngine.scan(uast);
        scanResult.issues.forEach(issue -> reportIssue(issue, context, inputFile));
        saveMetrics(scanResult.metrics, context, inputFile);
        saveHighlighting(uast, context, inputFile);
        saveCpdTokens(uast, context, inputFile);
      } catch (Exception e) {
        failedFiles.add(inputFile);
        LOG.debug("Error analyzing file " + inputFile.toString(), e);
      }
    }
    if (!failedFiles.isEmpty()) {
      String failedFilesAsString = failedFiles.stream().map(InputFile::toString).collect(Collectors.joining("\n"));
      LOG.error("Failed to analyze {} file(s). Turn on debug message to see the details. Failed files:\n{}", failedFiles.size(), failedFilesAsString);
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

  private static void saveCpdTokens(UastNode uast, SensorContext context, InputFile inputFile) {
    CpdVisitor cpdVisitor = new CpdVisitor(context, inputFile);
    cpdVisitor.scan(uast);
    cpdVisitor.save();
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
