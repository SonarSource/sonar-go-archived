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
package org.sonar.commonruleengine.checks;

import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.uast.UastNode;

@Rule(key = "S1151")
public class SwitchCaseTooBigCheck extends Check {

  private static final int DEFAULT_MAXIMUM = 5;

  @RuleProperty(
    key = "max",
    description = "Maximum number of lines",
    defaultValue = "" + DEFAULT_MAXIMUM)
  public int max = DEFAULT_MAXIMUM;

  public SwitchCaseTooBigCheck() {
    super(UastNode.Kind.CASE, UastNode.Kind.DEFAULT_CASE);
  }

  @Override
  public void visitNode(UastNode node) {
    int caseLength = node.lastToken().line - node.firstToken().line;
    if (caseLength > max) {
      node.getChild(UastNode.Kind.KEYWORD).ifPresent(caseKeyword -> reportIssue(caseKeyword,
        "Reduce this switch case number of lines from " + caseLength + " to at most " + max
          + ", for example by extracting code into methods."));
    }
  }
}
