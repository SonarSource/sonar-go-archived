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
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import static com.sonar.it.go.Tests.getMeasureAsInt;
import static org.assertj.core.api.Assertions.assertThat;

public class GoTestReportTest {

  @ClassRule
  public static Orchestrator orchestrator = Tests.ORCHESTRATOR;

  private static final String PIVOT_TEST = "project:pivot_test.go";

  @BeforeClass
  public static void startServer() {
    orchestrator.resetData();

    SonarScanner build = SonarScanner.create()
      .setProjectKey("project")
      .setProjectName("project")
      .setProjectVersion("1")
      .setSourceEncoding("UTF-8")
      .setSourceDirs(".")
      .setProperty("sonar.tests", ".")
      .setProperty("sonar.test.inclusions", "**/*_test.go")
      .setProjectDir(Tests.projectDirectoryFor("samples"))
      .setProperty("sonar.go.tests.reportPaths", Tests.projectDirectoryFor("samples/go-test-report.out").getAbsolutePath());

    orchestrator.executeBuild(build);
  }

  @Test
  public void project_metrics() {
    assertThat(getMeasureAsInt("project", "tests")).isEqualTo(4);
    assertThat(getMeasureAsInt("project", "test_failures")).isEqualTo(2);
    assertThat(getMeasureAsInt("project", "test_errors")).isNull();
    assertThat(getMeasureAsInt("project", "skipped_tests")).isEqualTo(1);
    assertThat(getMeasureAsInt("project", "test_execution_time")).isEqualTo(4);
  }

  @Test
  public void file_metrics() {
    assertThat(getMeasureAsInt(PIVOT_TEST, "tests")).isEqualTo(4);
    assertThat(getMeasureAsInt(PIVOT_TEST, "test_failures")).isEqualTo(2);
    assertThat(getMeasureAsInt(PIVOT_TEST, "test_errors")).isEqualTo(0);
    assertThat(getMeasureAsInt(PIVOT_TEST, "skipped_tests")).isEqualTo(1);
    assertThat(getMeasureAsInt(PIVOT_TEST, "test_execution_time")).isEqualTo(4);
  }

}
