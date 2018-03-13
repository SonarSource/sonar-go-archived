package org.sonar.go.plugin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.rule.internal.NewActiveRule;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.rule.RuleKey;
import org.sonar.commonruleengine.checks.Check;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class GoSensorTest {

  private Path workDir;
  private Path projectDir;
  private SensorContextTester sensorContext;

  @BeforeEach
  void setUp() throws IOException {
    workDir = Files.createTempDirectory("gotest");
    workDir.toFile().deleteOnExit();
    projectDir = Files.createTempDirectory("gotestProject");
    projectDir.toFile().deleteOnExit();
    sensorContext = SensorContextTester.create(workDir);
    sensorContext.fileSystem().setWorkDir(workDir);
  }

  @Test
  void test_description() {
    DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor();

    new GoSensor(getCheckFactory("S2068")).describe(descriptor);
    assertThat(descriptor.name()).isEqualTo("SonarGo");
    assertThat(descriptor.languages()).containsOnly("go");
  }

  @Test
  void test() throws IOException {
    InputFile inputFile = createInputFile("lets.go",
      "package main \n" +
        "\n" +
        "func test() {\n" +
        " pwd := \"secret\"\n" +
        "}");
    sensorContext.fileSystem().add(inputFile);
    GoSensor goSensor = new GoSensor(getCheckFactory("S2068"));
    goSensor.execute(sensorContext);
    assertThat(sensorContext.allIssues()).hasSize(1);
  }

  @Test
  void test_failure() throws Exception {
    InputFile failingFile = createInputFile("lets.go",
      "package main \n" +
        "\n" +
        "func test() {\n" +
        " pwd := \"secret\"\n" +
        "}");
    failingFile = spy(failingFile);
    IOException ioException = new IOException();
    when(failingFile.inputStream()).thenThrow(ioException);

    sensorContext.fileSystem().add(failingFile);
    GoSensor goSensor = new GoSensor(getCheckFactory("S2068"));
    assertThatThrownBy(() -> goSensor.execute(sensorContext))
      .isInstanceOf(GoSensor.GoPluginException.class)
      .hasMessage("Error during analysis. Last analyzed file: \"lets.go\"")
      .hasCause(ioException);
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

  private InputFile createInputFile(String filename, String content) throws IOException {
    Path filePath = projectDir.resolve(filename);
    return TestInputFileBuilder.create("module", projectDir.toFile(), filePath.toFile())
      .setCharset(StandardCharsets.UTF_8)
      .setLanguage(GoLanguage.KEY)
      .setContents(content)
      .build();
  }
}
