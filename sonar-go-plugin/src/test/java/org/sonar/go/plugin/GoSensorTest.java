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
import javax.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.rule.internal.NewActiveRule;
import org.sonar.api.batch.sensor.highlighting.TypeOfText;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.LoggerLevel;
import org.sonar.commonruleengine.checks.Check;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class GoSensorTest {

  @RegisterExtension
  static JUnit5LogTester logTester = new JUnit5LogTester();

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
    goSensor.execute(sensorContext);
    assertThat(logTester.logs(LoggerLevel.ERROR)).contains("Failed to analyze 1 file(s). Turn on debug message to see the details. Failed files:\nlets.go");
  }

  @Test
  void test_failure_empty_file() throws Exception {
    InputFile failingFile = createInputFile("lets.go", "");
    sensorContext.fileSystem().add(failingFile);
    GoSensor goSensor = getSensor("S2068");
    goSensor.execute(sensorContext);

    assertThat(logTester.logs(LoggerLevel.ERROR)).contains("Failed to analyze 1 file(s). Turn on debug message to see the details. Failed files:\nlets.go");
    assertThat(logTester.logs(LoggerLevel.DEBUG)).contains("Error analyzing file lets.go");
    // test log from external process asynchronously
    await().until(() -> logTester.logs(LoggerLevel.DEBUG).contains("panic: -:1:1: expected 'package', found 'EOF'"));
  }

  @Test
  void test_workdir_failure() {
    GoSensor goSensor = getSensor("S2068");
    assertThatThrownBy(() -> goSensor.execute(SensorContextTester.create(workDir)))
      .isInstanceOf(GoSensor.GoPluginException.class)
      .hasMessage("Error initializing UAST generator");
  }

  @Test
  public void metrics() throws Exception {
    InputFile inputFile = createInputFile("lets.go",
      "// This is not a line of code\n" +
        "package main\n" +
        "import \"fmt\"\n" +
        "type class1 struct { x, y int }\n" +
        "type class2 struct { a, b string }\n" +
        "type anyObject interface {}\n" +
        "func fun1() {\n" +
        "  fmt.Println(\"Statement 1\")\n" +
        "}\n" +
        "func fun2() {\n" +
        "  if true { // Statement 2\n" +
        "    fmt.Println(\"Statement 3\")\n" +
        "  }\n" +
        "}\n" +
        "func fun3(x interface{}) int {\n" +
        "  return 42 // Statement 4\n" +
        "}\n");
    sensorContext.fileSystem().add(inputFile);
    GoSensor goSensor = getSensor();
    goSensor.execute(sensorContext);
    assertThat(sensorContext.allIssues()).hasSize(0);
    assertThat(sensorContext.measure(inputFile.key(), CoreMetrics.NCLOC).value()).isEqualTo(16);
    assertThat(sensorContext.measure(inputFile.key(), CoreMetrics.COMMENT_LINES).value()).isEqualTo(3);
    assertThat(sensorContext.measure(inputFile.key(), CoreMetrics.CLASSES).value()).isEqualTo(2);
    assertThat(sensorContext.measure(inputFile.key(), CoreMetrics.FUNCTIONS).value()).isEqualTo(3);
    assertThat(sensorContext.measure(inputFile.key(), CoreMetrics.STATEMENTS).value()).isEqualTo(4);
  }

  @Test
  public void highlighting() throws Exception {
    InputFile inputFile = createInputFile("lets.go",
      "//abc\n" +
        "/*x*/\n" +
        "package main\n" +
        "import \"fmt\"\n" +
        "type class1 struct { }\n" +
        "func fun2(x string) int {\n" +
        "  return 42\n" +
        "}\n");
    sensorContext.fileSystem().add(inputFile);
    GoSensor goSensor = getSensor();
    goSensor.execute(sensorContext);

    String componentKey = "module:lets.go";
    // //abc
    assertHighlighting(componentKey, 1, 1, 5, TypeOfText.COMMENT);
    // /*x*/
    assertHighlighting(componentKey, 2, 1, 5, TypeOfText.STRUCTURED_COMMENT);
    // package main
    assertHighlighting(componentKey, 3, 1, 7, TypeOfText.KEYWORD);
    assertHighlighting(componentKey, 3, 9, 12, null);
    // import "fmt"
    assertHighlighting(componentKey, 4, 1, 6, TypeOfText.KEYWORD);
    assertHighlighting(componentKey, 4, 8, 12, TypeOfText.STRING);
    // type class1 struct { }
    assertHighlighting(componentKey, 5, 1, 4, TypeOfText.KEYWORD);
    assertHighlighting(componentKey, 5, 6, 11, null);
    assertHighlighting(componentKey, 5, 13, 18, TypeOfText.KEYWORD);
    assertHighlighting(componentKey, 5, 20, 22, null);
    // func fun2(x string) int {
    assertHighlighting(componentKey, 6, 1, 4, TypeOfText.KEYWORD);
    assertHighlighting(componentKey, 6, 6, 9, null);
    assertHighlighting(componentKey, 6, 6, 12, null);
    assertHighlighting(componentKey, 6, 13, 18, TypeOfText.KEYWORD_LIGHT);
    assertHighlighting(componentKey, 6, 19, 20, null);
    assertHighlighting(componentKey, 6, 21, 23, TypeOfText.KEYWORD_LIGHT);
    //   return 42
    assertHighlighting(componentKey, 7, 3, 8, TypeOfText.KEYWORD);
    assertHighlighting(componentKey, 7, 10, 11, TypeOfText.CONSTANT);
  }

  private void assertHighlighting(String componentKey, int line, int columnFirst, int columnLast, @Nullable TypeOfText type) {
    for (int column = columnFirst; column <= columnLast; column++) {
      List<TypeOfText> typeOfTexts = sensorContext.highlightingTypeAt(componentKey, line, column - 1);
      if (type != null) {
        assertThat(typeOfTexts).as("Expect highlighting " + type + " at line " + line + " lineOffset " + column).containsExactly(type);
      } else {
        assertThat(typeOfTexts).as("Expect no highlighting at line " + line + " lineOffset " + column).containsExactly();
      }
    }
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
