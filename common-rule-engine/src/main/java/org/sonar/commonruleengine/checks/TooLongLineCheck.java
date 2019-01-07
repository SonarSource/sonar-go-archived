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
package org.sonar.commonruleengine.checks;

import java.io.IOException;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.uast.UastNode;

@Rule(key = "S103")
public class TooLongLineCheck extends Check {

  private static final int DEFAULT_MAXIMUM = 120;
  private static final String MESSAGE = "Split this %s characters long line (which is greater than %s authorized).";

  @RuleProperty(
    key = "maximumLineLength",
    description = "The maximum authorized line length.",
    defaultValue = "" + DEFAULT_MAXIMUM)
  public int maximum = DEFAULT_MAXIMUM;

  @Override
  public void enterFile(InputFile inputFile) throws IOException {
    String[] lines = inputFile.contents().split("\\r?\\n");
    for (int i = 0; i < lines.length; i++) {
      String line = lines[i];
      if (line.length() > maximum) {
        String message = String.format(MESSAGE, line.length(), maximum);
        reportIssueOnLine(i + 1, message);
      }
    }
  }

  @Override
  public void visitNode(UastNode node) {
    // not used
  }

}
