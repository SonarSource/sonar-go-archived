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

import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.uast.UastNode;

@Rule(key = "S104")
public class TooManyLinesInFileCheck extends Check {

  private static final int DEFAULT_MAXIMUM = 750;

  @RuleProperty(
    key = "Max",
    description = "Maximum authorized lines in a file.",
    defaultValue = "" + DEFAULT_MAXIMUM)
  public int maximum = DEFAULT_MAXIMUM;

  public TooManyLinesInFileCheck() {
    super(UastNode.Kind.EOF);
  }

  @Override
  public void visitNode(UastNode node) {
    if (node.token.line > maximum) {
      reportIssueOnFile("This file has " + node.token.line + " lines, which is greater than " + maximum + " authorized. Split it into smaller files.");
    }
  }
}
