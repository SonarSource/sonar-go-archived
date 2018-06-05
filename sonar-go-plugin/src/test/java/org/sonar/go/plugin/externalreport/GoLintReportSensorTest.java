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
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sonar.api.batch.rule.Severity;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.ExternalIssue;
import org.sonar.api.rules.RuleType;
import org.sonar.api.utils.log.LoggerLevel;
import org.sonar.go.plugin.JUnit5LogTester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.go.plugin.externalreport.AbstractReportSensor.GENERIC_ISSUE_KEY;
import static org.sonar.go.plugin.externalreport.ExternalLinterSensorHelper.REPORT_BASE_PATH;

class GoLintReportSensorTest {

  @RegisterExtension
  static JUnit5LogTester logTester = new JUnit5LogTester();

  @BeforeEach
  void setUp() {
    logTester.clear();
  }

  @Test
  public void test_descriptor() {
    DefaultSensorDescriptor sensorDescriptor = new DefaultSensorDescriptor();
    new GoLintReportSensor().describe(sensorDescriptor);
    assertThat(sensorDescriptor.name()).isEqualTo("Import of Golint issues");
    assertThat(sensorDescriptor.languages()).containsOnly("go");
  }

  @Test
  void no_issues_with_sonarqube_71() throws IOException {
    SensorContextTester context = ExternalLinterSensorHelper.createContext(7, 1);
    context.settings().setProperty("sonar.go.golint.reportPaths", REPORT_BASE_PATH.resolve("golint-report.txt").toString());
    List<ExternalIssue> externalIssues = ExternalLinterSensorHelper.executeSensor(new GoLintReportSensor(), context);
    assertThat(externalIssues).isEmpty();
    assertThat(logTester.logs(LoggerLevel.ERROR)).containsExactly("GoLintReportSensor: Import of external issues requires SonarQube 7.2 or greater.");
  }

  @Test
  void issues_with_sonarqube_72() throws IOException {
    SensorContextTester context = ExternalLinterSensorHelper.createContext(7, 2);
    context.settings().setProperty("sonar.go.golint.reportPaths", REPORT_BASE_PATH.resolve("golint-report.txt").toString());
    List<ExternalIssue> externalIssues = ExternalLinterSensorHelper.executeSensor(new GoLintReportSensor(), context);
    assertThat(externalIssues).hasSize(2);

    ExternalIssue first = externalIssues.get(0);
    assertThat(first.type()).isEqualTo(RuleType.CODE_SMELL);
    assertThat(first.severity()).isEqualTo(Severity.MAJOR);
    assertThat(first.ruleKey().repository()).isEqualTo("golint");
    assertThat(first.ruleKey().rule()).isEqualTo("PackageComment");
    assertThat(first.primaryLocation().message()).isEqualTo("package comment should be of the form \"Package samples ...\"");
    assertThat(first.primaryLocation().textRange().start().line()).isEqualTo(1);

    ExternalIssue second = externalIssues.get(1);
    assertThat(second.type()).isEqualTo(RuleType.CODE_SMELL);
    assertThat(second.severity()).isEqualTo(Severity.MAJOR);
    assertThat(second.ruleKey().repository()).isEqualTo("golint");
    assertThat(second.ruleKey().rule()).isEqualTo("Exported");
    assertThat(second.primaryLocation().message()).isEqualTo("exported type User should have comment or be unexported");
    assertThat(second.primaryLocation().textRange().start().line()).isEqualTo(2);

    assertThat(logTester.logs(LoggerLevel.ERROR)).isEmpty();
  }

  @Test
  void no_issues_without_golint_property() throws IOException {
    SensorContextTester context = ExternalLinterSensorHelper.createContext(7, 2);
    List<ExternalIssue> externalIssues = ExternalLinterSensorHelper.executeSensor(new GoLintReportSensor(), context);
    assertThat(externalIssues).isEmpty();
    assertThat(logTester.logs(LoggerLevel.ERROR)).isEmpty();
  }

  @Test
  void no_issues_with_invalid_report_path() throws IOException {
    SensorContextTester context = ExternalLinterSensorHelper.createContext(7, 2);
    context.settings().setProperty("sonar.go.golint.reportPaths", REPORT_BASE_PATH.resolve("invalid-path.txt").toString());
    List<ExternalIssue> externalIssues = ExternalLinterSensorHelper.executeSensor(new GoLintReportSensor(), context);
    assertThat(externalIssues).isEmpty();
    assertThat(logTester.logs(LoggerLevel.ERROR)).hasSize(1);
    assertThat(logTester.logs(LoggerLevel.ERROR).get(0)).startsWith("GoLintReportSensor: No issues information will be saved as the report file");
  }

  @Test
  void no_issues_with_invalid_report_line() throws IOException {
    SensorContextTester context = ExternalLinterSensorHelper.createContext(7, 2);
    context.settings().setProperty("sonar.go.golint.reportPaths", REPORT_BASE_PATH.resolve("golint-report-with-error.txt").toString());
    List<ExternalIssue> externalIssues = ExternalLinterSensorHelper.executeSensor(new GoLintReportSensor(), context);
    assertThat(externalIssues).hasSize(1);
    assertThat(logTester.logs(LoggerLevel.ERROR)).hasSize(0);
    assertThat(logTester.logs(LoggerLevel.DEBUG)).hasSize(1);
    assertThat(logTester.logs(LoggerLevel.DEBUG).get(0)).startsWith("GoLintReportSensor: Unexpected line: xyz");
  }

  @Test
  void should_parse_golint_report_line() {
    String line = "./vendor/github.com/foo/go-bar/hello_world.go:550:12: redundant or: n == 2 || n == 2";
    org.sonar.go.plugin.externalreport.ExternalIssue issue = new GoLintReportSensor().parse(line);
    assertThat(issue).isNotNull();
    assertThat(issue.linter).isEqualTo("golint");
    assertThat(issue.type).isEqualTo(RuleType.CODE_SMELL);
    assertThat(issue.ruleKey).isEqualTo(GENERIC_ISSUE_KEY);
    assertThat(issue.filename).isEqualTo("./vendor/github.com/foo/go-bar/hello_world.go");
    assertThat(issue.lineNumber).isEqualTo(550);
    assertThat(issue.message).isEqualTo("redundant or: n == 2 || n == 2");
  }

  @Test
  void should_match_golint_all_keys() throws IOException {
    SensorContextTester context = ExternalLinterSensorHelper.createContext(7, 2);
    context.settings().setProperty("sonar.go.golint.reportPaths", REPORT_BASE_PATH.resolve("all-golint-report.txt").toString());
    // all 102 messages from report are parsed correctly
    List<ExternalIssue> externalIssues = ExternalLinterSensorHelper.executeSensor(new GoLintReportSensor(), context);
    assertThat(externalIssues).hasSize(102);
    // 17 distinct rule keys are present in the report
    Stream<String> uniqueKeys = externalIssues.stream().map(externalIssue -> externalIssue.ruleKey().rule()).distinct();
    assertThat(uniqueKeys).hasSize(17);
    // all messages are associated to a rule key
    Stream<ExternalIssue> notMatchedKeys = externalIssues.stream()
      .filter(externalIssue -> externalIssue.ruleKey().rule().equals(GENERIC_ISSUE_KEY));
    assertThat(notMatchedKeys).hasSize(0);
  }

  @Test
  void should_match_to_generic_issue_if_match_not_found() throws IOException {
    SensorContextTester context = ExternalLinterSensorHelper.createContext(7, 2);
    context.settings().setProperty("sonar.go.golint.reportPaths", REPORT_BASE_PATH.resolve("golint-with-unknown-message.txt").toString());
    List<ExternalIssue> externalIssues = ExternalLinterSensorHelper.executeSensor(new GoLintReportSensor(), context);
    assertThat(externalIssues.get(0).ruleKey().rule()).isEqualTo(GENERIC_ISSUE_KEY);
  }

}
