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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import static java.nio.charset.StandardCharsets.UTF_8;

public class GoVetReportSensor extends AbstractReportSensor {
  private static final Logger LOG = Loggers.get(GoVetReportSensor.class);

  public static final String PROPERTY_KEY = "sonar.go.govet.reportPaths";

  private static final Pattern GOVET_LINE_REGEX = Pattern.compile("(?<file>[^:]+):(?<line>\\d+):(?<message>.*)");

  @Override
  String linterName() {
    return "govet";
  }

  @Override
  String reportsPropertyName() {
    return PROPERTY_KEY;
  }

  @Override
  void importReport(File report, SensorContext context) {
    LOG.info("Importing {}", report.getAbsoluteFile());
    try {
      for (GoVetError goVetError : fromGovetFormat(Files.readAllLines(report.toPath(), UTF_8))) {
        addLineIssue(context, goVetError.filename, goVetError.lineNumber, goVetError.message);
      }
    } catch (IOException e) {
      LOG.error("No issues information will be saved as the report file '{}' can't be read.", report.getPath(), e);
    }
  }

  static List<GoVetError> fromGovetFormat(List<String> lines) {
    ArrayList<GoVetError> goVetErrors = new ArrayList<>();
    for (String line : lines) {
      Matcher matcher = GOVET_LINE_REGEX.matcher(line);
      if (matcher.matches()) {
        GoVetError govetError = new GoVetError();
        govetError.filename = matcher.group("file").trim();
        govetError.lineNumber = Integer.parseInt(matcher.group("line").trim());
        govetError.message = matcher.group("message").trim();
        goVetErrors.add(govetError);
      }
    }
    return goVetErrors;
  }

  static class GoVetError {
    int lineNumber;
    String message;
    String filename;
  }
}
