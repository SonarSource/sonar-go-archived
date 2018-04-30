/*
 * SonarQube Go Plugin
 * Copyright (C) 2018-2018 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.go.plugin;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.measure.Metric;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
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
import org.sonar.uast.validators.Validator;

import static org.sonar.go.plugin.GoCoverageReport.saveCoverageReports;
import static org.sonar.go.plugin.utils.PluginApiUtils.newRange;

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
      LOG.error("Error initializing UAST generator", e);
      return;
    }

    for (InputFile inputFile : getInputFiles(context)) {
      try (InputStream inputStream = inputFile.inputStream()) {
        UastNode uast = uastGenerator.createUast(inputStream);
        // FIXME currently *_test.go files are MAIN and not TEST, see issue #140
        if (inputFile.type() == InputFile.Type.MAIN) {
          Engine.ScanResult scanResult = ruleEngine.scan(uast, inputFile);
          scanResult.issues.forEach(issue -> reportIssue(issue, context, inputFile));
          saveMetrics(scanResult.metrics, context, inputFile);
          saveCpdTokens(uast, context, inputFile);
        }
        saveHighlighting(uast, context, inputFile);
      } catch (Validator.ValidationException e) {
        LOG.error("Unable to validate UAST of file " + inputFile.toString(), e);
      } catch (Exception e) {
        LOG.error("Error analyzing file " + inputFile.toString(), e);
      }
    }
    try {
      saveCoverageReports(context, GoCoverageReport.GoContext.DEFAULT);
    } catch (Exception e) {
      LOG.error("Coverage import failed: {}", e.getMessage(), e);
    }
  }

  private void reportIssue(Issue issue, SensorContext context, InputFile inputFile) {
    // TODO improve common rule engine to handle this out of the box
    RuleKey ruleKey = checks.ruleKey(issue.getCheck());
    Objects.requireNonNull(ruleKey, "Rule key not found for " + issue.getCheck().getClass());
    NewIssue newIssue = context.newIssue();
    NewIssueLocation location = newIssue.newLocation()
      .on(inputFile)
      .message(issue.getMessage());
    if (issue.hasNodeLocation()) {
      location.at(newRange(inputFile, issue.getPrimary().from, issue.getPrimary().to));
    } else if (issue.hasLineLocation()) {
      location.at(inputFile.selectLine(issue.getPrimary().line));
    }
    newIssue.forRule(ruleKey).at(location).gap(issue.getEffortToFix());

    Arrays.stream(issue.getSecondaries()).forEach(secondary -> newIssue.addLocation(
      newIssue.newLocation()
        .on(inputFile)
        .at(newRange(inputFile, secondary.from, secondary.to))
        .message(secondary.description == null ? "" : secondary.description)));

    newIssue.save();
  }

  private void saveMetrics(Metrics metrics, SensorContext context, InputFile inputFile) {
    saveMetric(context, inputFile, CoreMetrics.NCLOC, metrics.linesOfCode.size());
    saveMetric(context, inputFile, CoreMetrics.COMMENT_LINES, metrics.commentLines.size());
    saveMetric(context, inputFile, CoreMetrics.CLASSES, metrics.numberOfClasses);
    saveMetric(context, inputFile, CoreMetrics.FUNCTIONS, metrics.numberOfFunctions);
    saveMetric(context, inputFile, CoreMetrics.STATEMENTS, metrics.numberOfStatements);
    saveMetric(context, inputFile, CoreMetrics.COGNITIVE_COMPLEXITY, metrics.cognitiveComplexity);

    FileLinesContext linesContext = fileLinesContextFactory.createFor(inputFile);
    saveLinesMetrics(linesContext, metrics.linesOfCode, CoreMetrics.NCLOC_DATA_KEY);
    saveLinesMetrics(linesContext, metrics.commentLines, CoreMetrics.COMMENT_LINES_DATA_KEY);
    saveLinesMetrics(linesContext, metrics.executableLines, CoreMetrics.EXECUTABLE_LINES_DATA_KEY);
    linesContext.save();
  }

  private static void saveLinesMetrics(FileLinesContext linesContext, Set<Integer> lines, String metricKey) {
    lines.forEach(line -> linesContext.setIntValue(metricKey, line, 1));
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
