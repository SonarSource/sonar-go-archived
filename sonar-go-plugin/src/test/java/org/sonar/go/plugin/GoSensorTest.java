package org.sonar.go.plugin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.rule.RuleKey;
import org.sonar.commonruleengine.checks.Check;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class GoSensorTest {

  private Path workDir;
  private Path projectDir;
  private SensorContextTester sensorContext;
  private FileLinesContextFactory fileLinesContextFactory = mock(FileLinesContextFactory.class);

  @BeforeEach
  void setUp() throws IOException {
    workDir = Files.createTempDirectory("gotest");
    workDir.toFile().deleteOnExit();
    projectDir = Files.createTempDirectory("gotestProject");
    projectDir.toFile().deleteOnExit();
    sensorContext = SensorContextTester.create(workDir);
    sensorContext.fileSystem().setWorkDir(workDir);
    FileLinesContext fileLinesContext = mock(FileLinesContext.class);
    when(fileLinesContextFactory.createFor(any(InputFile.class))).thenReturn(fileLinesContext);
  }

  @Test
  void test_description() {
    DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor();

    getSensor("S2068").describe(descriptor);
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
    GoSensor goSensor = getSensor("S2068");
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
    GoSensor goSensor = getSensor("S2068");
    assertThatThrownBy(() -> goSensor.execute(sensorContext))
      .isInstanceOf(GoSensor.GoPluginException.class)
      .hasMessage("Error during analysis. Last analyzed file: \"lets.go\"")
      .hasCause(ioException);
  }

  @Test
  public void metrics() throws Exception {
    InputFile inputFile = createInputFile("lets.go",
      "// This is not a line of code\n" +
        "package main\n" +
        "import \"fmt\"\n" +
        "type class1 struct { }\n" +
        "type class2 struct { }\n" +
        "func fun1() {\n" +
        "  fmt.Println(\"Statement 1\")\n" +
        "}\n" +
        "func fun2() {\n" +
        "  if true { // Statement 2\n" +
        "    fmt.Println(\"Statement 3\")\n" +
        "  }\n" +
        "}\n" +
        "func fun3() int {\n" +
        "  return 42 // Statement 4\n" +
        "}\n");
    sensorContext.fileSystem().add(inputFile);
    GoSensor goSensor = getSensor();
    goSensor.execute(sensorContext);
    assertThat(sensorContext.allIssues()).hasSize(0);
    assertThat(sensorContext.measure(inputFile.key(), CoreMetrics.NCLOC).value()).isEqualTo(15);
    assertThat(sensorContext.measure(inputFile.key(), CoreMetrics.COMMENT_LINES).value()).isEqualTo(3);
    assertThat(sensorContext.measure(inputFile.key(), CoreMetrics.CLASSES).value()).isEqualTo(2);
    assertThat(sensorContext.measure(inputFile.key(), CoreMetrics.FUNCTIONS).value()).isEqualTo(3);
    assertThat(sensorContext.measure(inputFile.key(), CoreMetrics.STATEMENTS).value()).isEqualTo(4);
  }

  private GoSensor getSensor(String... activeRuleArray) {
    Set<String> activeRuleSet = new HashSet<>(Arrays.asList(activeRuleArray));
    List<Class> ruleClasses = GoChecks.getChecks();
    List<String> allKeys = ruleClasses.stream().map(ruleClass -> ((org.sonar.check.Rule) ruleClass.getAnnotations()[0]).key()).collect(Collectors.toList());
    ActiveRulesBuilder rulesBuilder = new ActiveRulesBuilder();
    allKeys.forEach(key -> {
      NewActiveRule newActiveRule = rulesBuilder.create(RuleKey.of(GoRulesDefinition.REPOSITORY_KEY, key));
      if (activeRuleSet.contains(key)) {
        newActiveRule.activate();
      }
    });
    ActiveRules activeRules = rulesBuilder.build();
    CheckFactory checkFactory = new CheckFactory(activeRules);
    Checks<Check> checks = checkFactory.create(GoRulesDefinition.REPOSITORY_KEY);
    checks.addAnnotatedChecks((Iterable) ruleClasses);
    return new GoSensor(checkFactory, fileLinesContextFactory);
  }

  private InputFile createInputFile(String filename, String content) {
    Path filePath = projectDir.resolve(filename);
    return TestInputFileBuilder.create("module", projectDir.toFile(), filePath.toFile())
      .setCharset(StandardCharsets.UTF_8)
      .setLanguage(GoLanguage.KEY)
      .setContents(content)
      .build();
  }
}
