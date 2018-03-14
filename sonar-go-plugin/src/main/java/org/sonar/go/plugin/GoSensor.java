package org.sonar.go.plugin;

import java.io.IOException;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.commonruleengine.Engine;
import org.sonar.commonruleengine.Issue;
import org.sonar.commonruleengine.checks.Check;
import org.sonar.uast.UastNode;

public class GoSensor implements Sensor {

  private final Checks<Check> checks;

  public GoSensor(CheckFactory checkFactory) {
    checks = checkFactory.<Check>create(GoRulesDefinition.REPOSITORY_KEY)
      .addAnnotatedChecks((Iterable) GoChecks.getChecks());
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
      }
    } catch (IOException e) {
      throw new GoPluginException("Error during analysis. Last analyzed file: \"" + lastAnalyzedFile + "\"", e);
    }
  }

  private void reportIssue(Issue issue, SensorContext context, InputFile inputFile) {
    // TODO improve common rule engine to handle this out of the box
    NewIssue newIssue = context.newIssue();
    TextRange textRange = inputFile.selectLine(issue.getLine());
    newIssue
      .at(newIssue.newLocation().on(inputFile).at(textRange).message(issue.getMessage()))
      .forRule(checks.ruleKey(issue.getRule()))
      .save();
  }

  private Iterable<InputFile> getInputFiles(SensorContext context) {
    FileSystem fs = context.fileSystem();
    return fs.inputFiles(fs.predicates().hasLanguage(GoLanguage.KEY));
  }

  static class GoPluginException extends RuntimeException {

    public GoPluginException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
