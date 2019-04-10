/*
 * SonarQube Go Plugin
 * Copyright (C) 2018-2019 SonarSource SA
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
package com.sonar.it.go;

import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.build.SonarScanner;
import java.util.HashSet;
import java.util.Set;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.sonar.wsclient.issue.IssueClient;
import org.sonar.wsclient.issue.IssueQuery;
import org.sonarqube.ws.Measures;

import static com.sonar.it.go.Tests.getMeasure;
import static com.sonar.it.go.Tests.getMeasureAsDouble;
import static org.assertj.core.api.Assertions.assertThat;

public class GoIntegrationTest {

  @ClassRule
  public static Orchestrator orchestrator = Tests.ORCHESTRATOR;

  private static final String PIVOT_SOURCE_FILE = "project:pivot.go";

  @BeforeClass
  public static void startServer() {
    orchestrator.resetData();

    SonarScanner build = SonarScanner.create()
      .setProjectKey("project")
      .setProjectName("project")
      .setProjectVersion("1")
      .setSourceEncoding("UTF-8")
      .setSourceDirs(".")
      .setProjectDir(Tests.projectDirectoryFor("samples"))
      .setProperty("sonar.go.coverage.reportPaths", Tests.projectDirectoryFor("samples/coverage.out").getAbsolutePath())
      ;

    orchestrator.executeBuild(build);
  }

  @Test
  public void issues() {
    IssueClient issueClient = orchestrator.getServer().wsClient().issueClient();
    assertThat(issueClient.find(IssueQuery.create().componentRoots("project")).list().size()).isGreaterThan(0);
  }

  @Test
  public void project_metrics() {
    // Size
    assertThat(getProjectMeasureAsDouble("ncloc")).isEqualTo(185d);
    assertThat(getProjectMeasureAsDouble("lines")).isEqualTo(242d);
    assertThat(getProjectMeasureAsDouble("files")).isEqualTo(12d);
    assertThat(getProjectMeasureAsDouble("classes")).isEqualTo(4d);
    assertThat(getProjectMeasureAsDouble("functions")).isEqualTo(21d);

    // Comments
    assertThat(getProjectMeasureAsDouble("comment_lines_density")).isEqualTo(7.5d);
    assertThat(getProjectMeasureAsDouble("comment_lines")).isEqualTo(15d);
    assertThat(getProjectMeasureAsDouble("public_documented_api_density")).isNull();
    assertThat(getProjectMeasureAsDouble("public_undocumented_api")).isNull();
    assertThat(getProjectMeasureAsDouble("public_api")).isNull();

    // Complexity
    assertThat(getProjectMeasureAsDouble("function_complexity")).isNull();
    assertThat(getProjectMeasureAsDouble("class_complexity")).isNull();
    assertThat(getProjectMeasureAsDouble("file_complexity")).isNull();
    assertThat(getProjectMeasureAsDouble("complexity")).isNull();
    assertThat(getProjectMeasureAsDouble("cognitive_complexity")).isEqualTo(16d);
    assertThat(getProjectMeasure("function_complexity_distribution")).isNull();
    assertThat(getProjectMeasure("file_complexity_distribution")).isNull();

    // Duplications
    assertThat(getProjectMeasureAsDouble("duplicated_lines")).isEqualTo(135d);
    assertThat(getProjectMeasureAsDouble("duplicated_blocks")).isEqualTo(5d);
    assertThat(getProjectMeasureAsDouble("duplicated_files")).isEqualTo(3d);
    assertThat(getProjectMeasureAsDouble("duplicated_lines_density")).isEqualTo(55.8d);
  }

  @Test
  public void file_metrics() {
    // Size
    assertThat(getFileMeasureAsDouble("ncloc")).isEqualTo(41d);
    assertThat(getFileMeasureAsDouble("lines")).isEqualTo(48d);
    assertThat(getFileMeasureAsDouble("files")).isEqualTo(1d);
    assertThat(getFileMeasureAsDouble("classes")).isEqualTo(1d);
    assertThat(getFileMeasureAsDouble("functions")).isEqualTo(3d);
    assertThat(lineNumbersInDataMeasure(getFileMeasure("ncloc_data").getValue())).isEqualTo(lineNumbersInDataMeasure(
      "1=1;3=1;4=1;5=1;6=1;7=1;8=1;10=1;11=1;12=1;13=1;14=1;16=1;17=1;18=1;20=1;21=1;22=1;23=1;24=1;25=1;26=1;27=1;28=1;29=1;30=1;31=1;32=1;33=1;35=1;36=1;37=1;38=1;39=1;40=1;41=1;43=1;44=1;45=1;46=1;47=1"));
    assertThat(lineNumbersInDataMeasure(getFileMeasure("executable_lines_data").getValue())).isEqualTo(lineNumbersInDataMeasure(
      "32=1;36=1;37=1;38=1;40=1;10=1;11=1;44=1;12=1;45=1;13=1;46=1;14=1;21=1;22=1;23=1;25=1;26=1;27=1;29=1;30=1"));
    assertThat(getFileMeasureAsDouble("lines_to_cover")).isEqualTo(16d);
    assertThat(getFileMeasureAsDouble("uncovered_lines")).isEqualTo(4d);

    // Comments
    assertThat(getFileMeasureAsDouble("comment_lines_density")).isEqualTo(0d);
    assertThat(getFileMeasureAsDouble("comment_lines")).isEqualTo(0d);
    assertThat(getFileMeasureAsDouble("public_documented_api_density")).isEqualTo(100);
    assertThat(getFileMeasureAsDouble("public_undocumented_api")).isZero();
    assertThat(getFileMeasureAsDouble("public_api")).isNull();

    // Complexity
    assertThat(getFileMeasureAsDouble("function_complexity")).isNull();
    assertThat(getFileMeasureAsDouble("class_complexity")).isNull();
    assertThat(getFileMeasureAsDouble("file_complexity")).isNull();
    assertThat(getFileMeasureAsDouble("complexity")).isNull();
    assertThat(getFileMeasureAsDouble("cognitive_complexity")).isEqualTo(4.0);

    // Duplications
    assertThat(getFileMeasureAsDouble("duplicated_lines")).isEqualTo(47d);
    assertThat(getFileMeasureAsDouble("duplicated_blocks")).isEqualTo(2d);
    assertThat(getFileMeasureAsDouble("duplicated_files")).isEqualTo(1d);
    assertThat(getFileMeasureAsDouble("duplicated_lines_density")).isEqualTo(97.9d);
  }

  private Set<Integer> lineNumbersInDataMeasure(String data) {
    Set<Integer> lineNumbers = new HashSet<>();
    for (String lineData : data.split(";")) {
      lineNumbers.add(Integer.valueOf(lineData.replace("=1", "")));
    }
    return lineNumbers;
  }

  private Measures.Measure getProjectMeasure(String metricKey) {
    return getMeasure("project", metricKey.trim());
  }

  private Double getProjectMeasureAsDouble(String metricKey) {
    return getMeasureAsDouble("project", metricKey.trim());
  }

  private Measures.Measure getFileMeasure(String metricKey) {
    return getMeasure(PIVOT_SOURCE_FILE, metricKey.trim());
  }

  private Double getFileMeasureAsDouble(String metricKey) {
    return getMeasureAsDouble(PIVOT_SOURCE_FILE, metricKey.trim());
  }
}
