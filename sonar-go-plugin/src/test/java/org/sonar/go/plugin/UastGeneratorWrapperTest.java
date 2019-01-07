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
package org.sonar.go.plugin;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.assertj.core.util.Files;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.uast.UastNode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UastGeneratorWrapperTest {

  private File workDir;

  @BeforeEach
  void setUp() {
    workDir = Files.temporaryFolder();
    workDir.deleteOnExit();
  }

  @Test
  void test() throws Exception {
    SensorContextTester sensorContext = SensorContextTester.create(workDir);
    sensorContext.fileSystem().setWorkDir(workDir.toPath());
    UastGeneratorWrapper generator = new UastGeneratorWrapper(sensorContext);
    ByteArrayInputStream in = new ByteArrayInputStream("package main\nfunc foo() {}".getBytes(StandardCharsets.UTF_8));

    UastNode uast = generator.createUast(in);
    assertThat(uast.joinTokens()).isEqualTo("package main\n" +
      "func foo() {}");
  }

  @Test
  void test_non_zero_return_value() throws Exception {
    String cp = System.getProperty("java.class.path");
    UastGeneratorWrapper generator = new UastGeneratorWrapper(() -> Arrays.asList("java", "-cp", cp, ExternalProcess.class.getCanonicalName(), "{kinds: []}", "2"));

    assertThatThrownBy(() -> generator.createUast(new ByteArrayInputStream(new byte[] {})))
      .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void test_stuck_process() throws Exception {
    String cp = System.getProperty("java.class.path");
    UastGeneratorWrapper generator = new UastGeneratorWrapper(() -> Arrays.asList("java", "-cp", cp, StuckExternalProcess.class.getCanonicalName(), "{kinds: []}"));

    assertThatThrownBy(() -> generator.createUast(new ByteArrayInputStream(new byte[] {})))
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("Took too long to parse. External process killed forcibly");
  }


}
