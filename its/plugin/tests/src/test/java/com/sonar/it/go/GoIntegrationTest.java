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
package com.sonar.it.go;

import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.build.SonarScanner;
import com.sonar.orchestrator.locator.FileLocation;
import java.util.HashSet;
import java.util.Set;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.sonarqube.ws.WsMeasures.Measure;

import static com.sonar.it.go.Tests.getMeasure;
import static com.sonar.it.go.Tests.getMeasureAsDouble;
import static org.assertj.core.api.Assertions.assertThat;

public class GoIntegrationTest {

  @ClassRule
  public static Orchestrator orchestrator = Tests.ORCHESTRATOR;

  private static final String FILE_TOKEN_PARSER = "project:OneStatementPerLine.go";

  @BeforeClass
  public static void startServer() {
    orchestrator.resetData();

    SonarScanner build = SonarScanner.create()
      .setProjectKey("project")
      .setProjectName("project")
      .setProjectVersion("1")
      .setSourceEncoding("UTF-8")
      .setSourceDirs(".")
      .setProjectDir(FileLocation.of("../../ruling/src/test/ruling-test-sources/samples").getFile())
      .setProfile("it-profile")
      ;

    orchestrator.executeBuild(build);
  }

  @Test
  public void projectMetric() {
    // Size
    assertThat(getProjectMeasureAsDouble("ncloc")).isEqualTo(56d);
    assertThat(getProjectMeasureAsDouble("lines")).isEqualTo(95d);
    assertThat(getProjectMeasureAsDouble("files")).isEqualTo(9d);
    assertThat(getProjectMeasureAsDouble("classes")).isEqualTo(1d);
    assertThat(getProjectMeasureAsDouble("functions")).isEqualTo(11d);

    // Comments
    assertThat(getProjectMeasureAsDouble("comment_lines_density")).isEqualTo(29.1);
    assertThat(getProjectMeasureAsDouble("comment_lines")).isEqualTo(23d);
    assertThat(getProjectMeasureAsDouble("public_documented_api_density")).isNull();
    assertThat(getProjectMeasureAsDouble("public_undocumented_api")).isNull();
    assertThat(getProjectMeasureAsDouble("public_api")).isNull();

    // Complexity
    assertThat(getProjectMeasureAsDouble("function_complexity")).isNull();
    assertThat(getProjectMeasureAsDouble("class_complexity")).isNull();
    assertThat(getProjectMeasureAsDouble("file_complexity")).isNull();
    assertThat(getProjectMeasureAsDouble("complexity")).isNull();
    assertThat(getProjectMeasureAsDouble("cognitive_complexity")).isEqualTo(8d);
    assertThat(getProjectMeasure("function_complexity_distribution")).isNull();
    assertThat(getProjectMeasure("file_complexity_distribution")).isNull();
    assertThat(getProjectMeasureAsDouble("class_complexity_distribution")).isNull();
  }

  @Test
  public void fileMetrics() {
    // Size
    assertThat(getFileMeasureAsDouble("ncloc")).isEqualTo(15d);
    assertThat(getFileMeasureAsDouble("lines")).isEqualTo(24d);
    assertThat(getFileMeasureAsDouble("files")).isEqualTo(1d);
    // TODO
    assertThat(getFileMeasureAsDouble("classes")).isEqualTo(0d);
    assertThat(getFileMeasureAsDouble("functions")).isEqualTo(2d);
    assertThat(lineNumbersInDataMeasure(getFileMeasure("ncloc_data").getValue())).isEqualTo(lineNumbersInDataMeasure(
      "2=1;4=1;6=1;8=1;10=1;11=1;13=1;14=1;16=1;17=1;18=1;19=1;21=1;22=1;23=1"));
    assertThat(lineNumbersInDataMeasure(getFileMeasure("executable_lines_data").getValue())).isEqualTo(lineNumbersInDataMeasure(
      "16=1;17=1;18=1;22=1;6=1;8=1;10=1;13=1;14=1"));
    assertThat(getFileMeasureAsDouble("lines_to_cover")).isEqualTo(9d);
    // TODO
    assertThat(getFileMeasureAsDouble("uncovered_lines")).isEqualTo(9d);

    // Comments
    assertThat(getFileMeasureAsDouble("comment_lines_density")).isEqualTo(34.8);
    assertThat(getFileMeasureAsDouble("comment_lines")).isEqualTo(8d);
    assertThat(getFileMeasureAsDouble("public_documented_api_density")).isEqualTo(100);
    assertThat(getFileMeasureAsDouble("public_undocumented_api")).isZero();

    assertThat(getFileMeasureAsDouble("public_api")).isNull();

    // Complexity
    assertThat(getFileMeasureAsDouble("function_complexity")).isNull();
    assertThat(getFileMeasureAsDouble("class_complexity")).isNull();
    assertThat(getFileMeasureAsDouble("file_complexity")).isNull();
    assertThat(getFileMeasureAsDouble("complexity")).isNull();
    assertThat(getFileMeasureAsDouble("cognitive_complexity")).isEqualTo(5.0);
  }

  private Set<Integer> lineNumbersInDataMeasure(String data) {
    Set<Integer> lineNumbers = new HashSet<>();
    for (String lineData : data.split(";")) {
      lineNumbers.add(Integer.valueOf(lineData.replace("=1", "")));
    }
    return lineNumbers;
  }

  @Test
  public void testDuplicationResults() {
    // TODO
    assertThat(getProjectMeasureAsDouble("duplicated_lines")).isEqualTo(0d);
    assertThat(getProjectMeasureAsDouble("duplicated_blocks")).isEqualTo(0d);
    assertThat(getProjectMeasureAsDouble("duplicated_files")).isEqualTo(0d);
    assertThat(getProjectMeasureAsDouble("duplicated_lines_density")).isEqualTo(0d);
  }

  private Measure getProjectMeasure(String metricKey) {
    return getMeasure("project", metricKey.trim());
  }

  private Double getProjectMeasureAsDouble(String metricKey) {
    return getMeasureAsDouble("project", metricKey.trim());
  }

  private Measure getFileMeasure(String metricKey) {
    return getMeasure(FILE_TOKEN_PARSER, metricKey.trim());
  }

  private Double getFileMeasureAsDouble(String metricKey) {
    return getMeasureAsDouble(FILE_TOKEN_PARSER, metricKey.trim());
  }
}
