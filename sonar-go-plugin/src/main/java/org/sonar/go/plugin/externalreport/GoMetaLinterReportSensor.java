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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.sonar.api.rules.RuleType;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class GoMetaLinterReportSensor extends AbstractReportSensor {

  private static final Logger LOG = Loggers.get(GoMetaLinterReportSensor.class);

  public static final String PROPERTY_KEY = "sonar.go.gometalinter.reportPaths";

  private static final Pattern GO_META_LINTER_REGEX = Pattern.compile("(?<file>[^:]+):(?<line>\\d+):\\d*:" +
    "(?<severity>(error|warning)):(?<message>.*)\\((?<linter>[^\\(]*)\\)");

  @Override
  String linterName() {
    return "GoMetaLinter";
  }

  @Override
  String reportsPropertyName() {
    return PROPERTY_KEY;
  }

  @Nullable
  @Override
  ExternalIssue parse(String line) {
    Matcher matcher = GO_META_LINTER_REGEX.matcher(line);
    if (matcher.matches()) {
      String linter = mapLinterName(matcher.group("linter").trim());
      RuleType type = "error".equals(matcher.group("severity")) ? RuleType.BUG : RuleType.CODE_SMELL;
      String filename = matcher.group("file").trim();
      int lineNumber = Integer.parseInt(matcher.group("line").trim());
      String message = matcher.group("message").trim();
      return new ExternalIssue(linter, type, GENERIC_ISSUE_KEY, filename, lineNumber, message);
    } else {
      LOG.debug(logPrefix() + "Unexpected line: " + line);
    }
    return null;
  }

  private static String mapLinterName(String linter) {
    if ("vet".equals(linter)) {
      return GoVetReportSensor.LINTER_ID;
    }
    return linter;
  }

}
