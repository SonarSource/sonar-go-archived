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
import org.junit.ClassRule;
import org.junit.Test;

import static com.sonar.it.go.Tests.getMeasureAsInt;
import static org.assertj.core.api.Assertions.assertThat;

public class GoTest {

  private static final String EMPTY_FILE_PROJET_KEY = "empty_file_project_key";

  @ClassRule
  public static Orchestrator orchestrator = Tests.ORCHESTRATOR;

  @Test
  public void should_not_fail_on_empty_file() {
    SonarScanner build = SonarScanner.create()
      .setProjectKey(EMPTY_FILE_PROJET_KEY)
      .setProjectName("Empty file test project")
      .setProjectVersion("1")
      .setSourceEncoding("UTF-8")
      .setSourceDirs(".")
      .setProjectDir(Tests.projectDirectoryFor("empty_file"));
    orchestrator.executeBuild(build);

    assertThat(getMeasureAsInt(EMPTY_FILE_PROJET_KEY, "files")).isEqualTo(2);
  }

}
