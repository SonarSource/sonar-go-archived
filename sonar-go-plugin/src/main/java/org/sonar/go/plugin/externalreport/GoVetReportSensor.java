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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.sonar.api.rules.RuleType;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class GoVetReportSensor extends AbstractReportSensor {

  private static final Logger LOG = Loggers.get(GoVetReportSensor.class);

  public static final String PROPERTY_KEY = "sonar.go.govet.reportPaths";

  private static final Pattern GO_VET_LINE_REGEX = Pattern.compile("(?<file>[^:]+):(?<line>\\d+):(?<message>.*)");

  public static final String LINTER_ID = "govet";
  public static final String LINTER_NAME = "go vet";

  @Override
  String linterName() {
    return LINTER_NAME;
  }

  @Override
  String reportsPropertyName() {
    return PROPERTY_KEY;
  }

  @Nullable
  @Override
  ExternalIssue parse(String line) {
    Matcher matcher = GO_VET_LINE_REGEX.matcher(line);
    if (matcher.matches()) {
      String filename = matcher.group("file").trim();
      int lineNumber = Integer.parseInt(matcher.group("line").trim());
      String message = matcher.group("message").trim();
      return new ExternalIssue(LINTER_ID, RuleType.BUG, null, filename, lineNumber, message);
    } else if (!line.startsWith("exit status")) {
      LOG.debug(logPrefix() + "Unexpected line: " + line);
    }
    return null;
  }

}
