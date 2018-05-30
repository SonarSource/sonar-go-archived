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
package org.sonar.go.plugin.externalreport;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.rule.Severity;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.ExternalIssue;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.utils.Version;
import org.sonar.api.utils.log.LoggerLevel;
import org.sonar.go.plugin.GoLanguage;
import org.sonar.go.plugin.JUnit5LogTester;

import static org.assertj.core.api.Assertions.assertThat;

class GoVetReportSensorTest {

  @RegisterExtension
  static JUnit5LogTester logTester = new JUnit5LogTester();

  private Path workDir;
  private Path projectDir;
  private SensorContextTester sensorContext;
  private InputFile mainInputFile;
  private GoVetReportSensor sensor;

  @BeforeEach
  void setUp() throws IOException {
    logTester.clear();
    workDir = Files.createTempDirectory("govettemp");
    workDir.toFile().deleteOnExit();
    projectDir = Files.createTempDirectory("govetproject");
    projectDir.toFile().deleteOnExit();
    sensorContext = SensorContextTester.create(workDir);
    sensorContext.fileSystem().setWorkDir(workDir);
    Path filePath = projectDir.resolve("main.go");
    mainInputFile = TestInputFileBuilder.create("module", projectDir.toFile(), filePath.toFile())
      .setCharset(StandardCharsets.UTF_8)
      .setLanguage(GoLanguage.KEY)
      .setContents("package main\n" +
        "import \"fmt\"\n" +
        "func main() {\n" +
        "  fmt.Println(\"Hello\")\n" +
        "}\n")
      .setType(InputFile.Type.MAIN)
      .build();
    sensorContext.fileSystem().add(mainInputFile);
    sensor = new GoVetReportSensor();
  }

  @Test
  public void test_descriptor() throws Exception {
    DefaultSensorDescriptor sensorDescriptor = new DefaultSensorDescriptor();
    sensor.describe(sensorDescriptor);
    assertThat(sensorDescriptor.name()).isEqualTo("Import of go vet issues");
    assertThat(sensorDescriptor.languages()).containsOnly("go");
  }

  @Test
  void no_issues_with_sonarqube_71() {
    List<ExternalIssue> externalIssues = executeSensor(7, 1, "govet-report.txt");
    assertThat(externalIssues).isEmpty();
    assertThat(logTester.logs(LoggerLevel.ERROR)).containsExactly("Import of external issues requires SonarQube 7.2 or greater.");
  }

  @Test
  void issues_with_sonarqube_72() {
    List<ExternalIssue> externalIssues = executeSensor(7, 2, "govet-report.txt");
    assertThat(externalIssues).hasSize(2);

    ExternalIssue first = externalIssues.get(0);
    assertThat(first.severity()).isEqualTo(Severity.MAJOR);
    assertThat(first.primaryLocation().message()).isEqualTo("comparison of function Foo == nil is always false");
    assertThat(first.primaryLocation().textRange().start().line()).isEqualTo(1);

    ExternalIssue second = externalIssues.get(1);
    assertThat(second.severity()).isEqualTo(Severity.MAJOR);
    assertThat(second.primaryLocation().message()).isEqualTo("Printf format %s has arg &str of wrong type *string");
    assertThat(second.primaryLocation().textRange().start().line()).isEqualTo(2);

    assertThat(logTester.logs(LoggerLevel.ERROR)).isEmpty();
  }

  @Test
  void no_issues_without_govet_property() {
    List<ExternalIssue> externalIssues = executeSensor(7, 2, null);
    assertThat(externalIssues).isEmpty();
    assertThat(logTester.logs(LoggerLevel.ERROR)).isEmpty();
  }

  @Test
  void no_issues_with_invalid_report_path() {
    List<ExternalIssue> externalIssues = executeSensor(7, 2, "invalid-path.txt");
    assertThat(externalIssues).isEmpty();
    assertThat(logTester.logs(LoggerLevel.ERROR)).hasSize(1);
    assertThat(logTester.logs(LoggerLevel.ERROR).get(0)).startsWith("No issues information will be saved as the report file");
  }

  @Test
  void should_parse_govet_report_line() {
    List<String> lines = Collections.singletonList("./vendor/github.com/foo/go-bar/hello_world.go:550: redundant or: n == 2 || n == 2");
    List<GoVetReportSensor.GoVetError> goVetErrors = GoVetReportSensor.fromGovetFormat(lines);
    assertThat(goVetErrors).hasSize(1);
    GoVetReportSensor.GoVetError goVetError = goVetErrors.get(0);
    assertThat(goVetError.filename).isEqualTo("./vendor/github.com/foo/go-bar/hello_world.go");
    assertThat(goVetError.lineNumber).isEqualTo(550);
    assertThat(goVetError.message).isEqualTo("redundant or: n == 2 || n == 2");
  }

  private List<ExternalIssue> executeSensor(int majorVersion, int minorVersion, @Nullable String reportFileName) {
    sensorContext.setRuntime(SonarRuntimeImpl.forSonarQube(Version.create(majorVersion, minorVersion), SonarQubeSide.SERVER));
    if (reportFileName != null) {
      Path reportPath = Paths.get("src", "test", "resources", "externalreport", reportFileName).toAbsolutePath();
      sensorContext.settings().setProperty("sonar.go.govet.reportPaths", reportPath.toString());
    }
    sensor.execute(sensorContext);
    return new ArrayList<>(sensorContext.allExternalIssues());
  }

}
