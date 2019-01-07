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
package org.sonar.go.plugin.externalreport;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.ExternalIssue;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.utils.Version;
import org.sonar.go.plugin.GoLanguage;

public class ExternalLinterSensorHelper {

  static final Path REPORT_BASE_PATH = Paths.get("src", "test", "resources", "externalreport").toAbsolutePath();

  static List<ExternalIssue> executeSensor(Sensor sensor, SensorContextTester context) {
    sensor.execute(context);
    return new ArrayList<>(context.allExternalIssues());
  }

  static SensorContextTester createContext(int majorVersion, int minorVersion) throws IOException {
    Path workDir = Files.createTempDirectory("gotemp");
    workDir.toFile().deleteOnExit();
    Path projectDir = Files.createTempDirectory("goproject");
    projectDir.toFile().deleteOnExit();
    SensorContextTester context = SensorContextTester.create(workDir);
    context.fileSystem().setWorkDir(workDir);
    Path filePath = projectDir.resolve("main.go");
    InputFile mainInputFile = TestInputFileBuilder.create("module", projectDir.toFile(), filePath.toFile())
      .setCharset(StandardCharsets.UTF_8)
      .setLanguage(GoLanguage.KEY)
      .setContents("package main\n" +
        "import \"fmt\"\n" +
        "func main() {\n" +
        "  fmt.Println(\"Hello\")\n" +
        "}\n")
      .setType(InputFile.Type.MAIN)
      .build();
    context.fileSystem().add(mainInputFile);
    context.setRuntime(SonarRuntimeImpl.forSonarQube(Version.create(majorVersion, minorVersion), SonarQubeSide.SERVER));
    return context;
  }

}
