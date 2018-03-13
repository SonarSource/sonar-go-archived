package org.sonar.go.plugin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.rule.internal.NewActiveRule;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.rule.RuleKey;
import org.sonar.commonruleengine.checks.Check;

import static org.assertj.core.api.Assertions.assertThat;

class GoSensorTest {

  private static Path workDir;
  private static Path projectDir;
  private static SensorContextTester sensorContext;

  @BeforeAll
  static void setUp() throws IOException {
    workDir = Files.createTempDirectory("gotest");
    workDir.toFile().deleteOnExit();
    projectDir = Files.createTempDirectory("gotestProject");
    projectDir.toFile().deleteOnExit();
    sensorContext = SensorContextTester.create(workDir);
    sensorContext.fileSystem().setWorkDir(workDir);
  }

  @Test
  void test() throws IOException {
    createInputFile("lets.go",
      "package main \n" +
      "\n" +
      "func test() {\n" +
      " pwd := \"secret\"\n" +
      "}");
    GoSensor goSensor = new GoSensor(getCheckFactory("S2068"));
    goSensor.execute(sensorContext);
    assertThat(sensorContext.allIssues()).hasSize(1);
  }

  private CheckFactory getCheckFactory(String activeRule) {
    List<Class> ruleClasses = GoChecks.getChecks();
    List<String> allKeys = ruleClasses.stream().map(ruleClass -> ((org.sonar.check.Rule) ruleClass.getAnnotations()[0]).key()).collect(Collectors.toList());
    ActiveRulesBuilder rulesBuilder = new ActiveRulesBuilder();
    allKeys.forEach(key -> {
      NewActiveRule newActiveRule = rulesBuilder.create(RuleKey.of(GoRulesDefinition.REPOSITORY_KEY, key));
      if (activeRule.equals(key)) {
        newActiveRule.activate();
      }
    });
    ActiveRules activeRules = rulesBuilder.build();
    CheckFactory checkFactory = new CheckFactory(activeRules);
    Checks<Check> checks = checkFactory.create(GoRulesDefinition.REPOSITORY_KEY);
    checks.addAnnotatedChecks(ruleClasses);
    return checkFactory;
  }

  private void createInputFile(String filename, String content) throws IOException {
    Path filePath = projectDir.resolve(filename);
    Files.write(filePath, content.getBytes(StandardCharsets.UTF_8));
    InputFile inputFile = TestInputFileBuilder.create("module", projectDir.toFile(), filePath.toFile())
      .setLanguage(GoLanguage.KEY)
      .setContents(content)
      .build();
    sensorContext.fileSystem().add(inputFile);
  }
}
