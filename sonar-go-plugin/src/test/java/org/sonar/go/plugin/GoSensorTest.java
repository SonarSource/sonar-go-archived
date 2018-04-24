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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
  private FileLinesContextTester fileLinesContext;

  @BeforeEach
  void setUp() throws IOException {
    workDir = Files.createTempDirectory("gotest");
    workDir.toFile().deleteOnExit();
    projectDir = Files.createTempDirectory("gotestProject");
    projectDir.toFile().deleteOnExit();
    sensorContext = SensorContextTester.create(workDir);
    sensorContext.fileSystem().setWorkDir(workDir);
    fileLinesContext = new FileLinesContextTester();
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
  void test_issue() throws IOException {
    InputFile inputFile = createInputFile("lets.go", InputFile.Type.MAIN,
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
  void test_file_issue() throws IOException {
    InputFile inputFile = createInputFile("lets.go", InputFile.Type.MAIN,
      "// hey \n package main \n");
    sensorContext.fileSystem().add(inputFile);
    GoSensor goSensor = getSensor("S1451");
    goSensor.execute(sensorContext);
    assertThat(sensorContext.allIssues()).hasSize(1);
  }

  @Test
  void test_line_issue() throws IOException {
    InputFile inputFile = createInputFile("lets.go", InputFile.Type.MAIN,
      "package                                                                                                                           main\n");
    sensorContext.fileSystem().add(inputFile);
    GoSensor goSensor = getSensor("S103");
    goSensor.execute(sensorContext);
    assertThat(sensorContext.allIssues()).hasSize(1);
  }

  @Test
  void test_failure() throws Exception {
    InputFile failingFile = createInputFile("lets.go", InputFile.Type.MAIN,
      "package main \n" +
        "\n" +
        "func test() {\n" +
        " pwd := \"secret\"\n" +
        "}");
    failingFile = spy(failingFile);
    IOException ioException = new IOException();
    when(failingFile.inputStream()).thenThrow(ioException);

    sensorContext.fileSystem().add(failingFile);
    sensorContext.settings().setProperty("sonar.go.coverage.reportPaths", "invalid-coverage-path.out");
    GoSensor goSensor = getSensor("S2068");
    goSensor.execute(sensorContext);
    assertThat(logTester.logs(LoggerLevel.ERROR).stream().collect(Collectors.joining("\n")))
      .contains("Failed to analyze 1 file(s). Turn on debug message to see the details. Failed files:\nlets.go")
      .contains("Coverage report can't be loaded, file not found:").contains("invalid-coverage-path.out");
  }

  @Test
  void test_failure_empty_file() throws Exception {
    InputFile failingFile = createInputFile("lets.go", InputFile.Type.MAIN, "");
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
    goSensor.execute(SensorContextTester.create(workDir));
    assertThat(logTester.logs(LoggerLevel.ERROR)).contains("Error initializing UAST generator");
  }

  @Test
  public void metrics() {
    InputFile inputFile = createInputFile("lets.go", InputFile.Type.MAIN,
      /* 01 */"// This is not a line of code\n" +
      /* 02 */"package main\n" +
      /* 03 */"import \"fmt\"\n" +
      /* 04 */"type class1 struct { x, y int }\n" +
      /* 05 */"type class2 struct { a, b string }\n" +
      /* 06 */"type anyObject interface {}\n" +
      /* 07 */"func fun1() {\n" +
      /* 08 */"  fmt.Println(\"Statement 1\")\n" +
      /* 09 */"}\n" +
      /* 10 */"func fun2(i int) {\n" +
      /* 11 */"  switch i { // Statement 2\n" +
      /* 12 */"  case 2:\n" +
      /* 13 */"    fmt.Println(\n" +
      /* 14 */"      \"Statement 3\",\n" +
      /* 15 */"    )\n" +
      /* 16 */"  }\n" +
      /* 17 */"}\n" +
      /* 18 */"func fun3(x interface{}) int {\n" +
      /* 19 */"  return 42 // Statement 4\n" +
      /* 20 */"}\n");
    sensorContext.fileSystem().add(inputFile);
    GoSensor goSensor = getSensor();
    goSensor.execute(sensorContext);
    assertThat(sensorContext.allIssues()).hasSize(0);
    assertThat(sensorContext.measure(inputFile.key(), CoreMetrics.NCLOC).value()).isEqualTo(19);
    assertThat(sensorContext.measure(inputFile.key(), CoreMetrics.COMMENT_LINES).value()).isEqualTo(3);
    assertThat(sensorContext.measure(inputFile.key(), CoreMetrics.CLASSES).value()).isEqualTo(2);
    assertThat(sensorContext.measure(inputFile.key(), CoreMetrics.FUNCTIONS).value()).isEqualTo(3);
    assertThat(sensorContext.measure(inputFile.key(), CoreMetrics.STATEMENTS).value()).isEqualTo(4);
    assertThat(sensorContext.measure(inputFile.key(), CoreMetrics.COGNITIVE_COMPLEXITY).value()).isEqualTo(1);

    assertThat(fileLinesContext.saveCount).isEqualTo(1);

    assertThat(fileLinesContext.metrics.keySet()).containsExactlyInAnyOrder(
      CoreMetrics.COMMENT_LINES_DATA_KEY, CoreMetrics.NCLOC_DATA_KEY, CoreMetrics.EXECUTABLE_LINES_DATA_KEY);

    assertThat(fileLinesContext.metrics.get(CoreMetrics.COMMENT_LINES_DATA_KEY)).containsExactlyInAnyOrder(
      "1:1", "11:1", "19:1");

    assertThat(fileLinesContext.metrics.get(CoreMetrics.NCLOC_DATA_KEY)).containsExactlyInAnyOrder(
      "2:1", "3:1", "4:1", "5:1", "6:1", "7:1", "8:1", "9:1", "10:1", "11:1",
      "12:1", "13:1", "14:1", "15:1", "16:1", "17:1", "18:1", "19:1", "20:1");

    assertThat(fileLinesContext.metrics.get(CoreMetrics.EXECUTABLE_LINES_DATA_KEY)).containsExactlyInAnyOrder(
      "8:1", "11:1", "12:1", "13:1", "14:1", "19:1");
  }

  @Test
  public void metrics_for_test_file() {
    InputFile inputFile = createInputFile("lets.go", InputFile.Type.TEST,
      "// This is not a line of code\n" +
        "package main\n" +
        "import \"fmt\"\n" +
        "func main() {\n" +
        "  fmt.Println(\"Hello\")\n" +
        "}\n");
    sensorContext.fileSystem().add(inputFile);
    GoSensor goSensor = getSensor();
    goSensor.execute(sensorContext);
    assertThat(sensorContext.allIssues()).hasSize(0);
    assertThat(sensorContext.measure(inputFile.key(), CoreMetrics.NCLOC)).isNull();
    assertThat(sensorContext.measure(inputFile.key(), CoreMetrics.COMMENT_LINES)).isNull();
    assertThat(sensorContext.measure(inputFile.key(), CoreMetrics.CLASSES)).isNull();
    assertThat(sensorContext.measure(inputFile.key(), CoreMetrics.FUNCTIONS)).isNull();
    assertThat(sensorContext.measure(inputFile.key(), CoreMetrics.STATEMENTS)).isNull();
    assertThat(sensorContext.measure(inputFile.key(), CoreMetrics.COGNITIVE_COMPLEXITY)).isNull();

    assertThat(fileLinesContext.saveCount).isEqualTo(0);
    assertThat(fileLinesContext.metrics.keySet()).isEmpty();
  }

  @Test
  public void cognitive_complexity_metric() {
    InputFile inputFile = createInputFile("lets.go", InputFile.Type.MAIN,
        "package main\n" +
        "import \"fmt\"\n" +
        "func fun1(i int) int {\n" +
        "  if i < 0 { // +1\n" +
        "    i++\n" +
        "  }\n" +
        "  return i\n" +
        "}\n" +
        "func fun2(i int) int {\n" +
        "  if i < 0 { // +1\n" +
        "    i--\n" +
        "  }\n" +
        "  f := func(int) int {\n" +
        "    if i < 0 { // +2 (incl 1 for nesting)\n" +
        "      i++\n" +
        "    }\n" +
        "    return i\n" +
        "  }\n" +
        "  return i + f(i)\n" +
        "}\n" +
        "\n");
    sensorContext.fileSystem().add(inputFile);
    GoSensor goSensor = getSensor();
    goSensor.execute(sensorContext);
    assertThat(sensorContext.measure(inputFile.key(), CoreMetrics.COGNITIVE_COMPLEXITY).value()).isEqualTo(4);
  }

  @Test
  public void highlighting() throws Exception {
    InputFile inputFile = createInputFile("lets.go", InputFile.Type.MAIN,
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
    // return 42
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
        if (key.equals("S1451")) {
          newActiveRule.setParam("headerFormat", "some header format");
        }
      }
    });
    ActiveRules activeRules = rulesBuilder.build();
    CheckFactory checkFactory = new CheckFactory(activeRules);
    Checks<Check> checks = checkFactory.create(GoRulesDefinition.REPOSITORY_KEY);
    checks.addAnnotatedChecks((Iterable) ruleClasses);
    return new GoSensor(checkFactory, fileLinesContextFactory);
  }

  private InputFile createInputFile(String filename, InputFile.Type type, String content) {
    Path filePath = projectDir.resolve(filename);
    return TestInputFileBuilder.create("module", projectDir.toFile(), filePath.toFile())
      .setCharset(StandardCharsets.UTF_8)
      .setLanguage(GoLanguage.KEY)
      .setContents(content)
      .setType(type)
      .build();
  }

  private static class FileLinesContextTester implements FileLinesContext {
    int saveCount = 0;
    Map<String, Set<String>> metrics = new HashMap<>();

    @Override
    public void setIntValue(String metricKey, int line, int value) {
      setStringValue(metricKey, line, String.valueOf(value));
    }

    @Override
    public Integer getIntValue(String metricKey, int line) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setStringValue(String metricKey, int line, String value) {
      metrics.computeIfAbsent(metricKey, key -> new HashSet<>())
        .add(line + ":" + value);
    }

    @Override
    public String getStringValue(String metricKey, int line) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void save() {
      saveCount++;
    }
  }
}
