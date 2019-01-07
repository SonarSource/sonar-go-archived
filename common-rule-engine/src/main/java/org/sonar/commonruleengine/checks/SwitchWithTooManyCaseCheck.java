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
import org.sonar.commonruleengine.Issue;
import org.sonar.uast.UastNode;
import org.sonar.uast.helpers.SwitchLike;

@Rule(key = "S1479")
public class SwitchWithTooManyCaseCheck extends Check {

  private static final int DEFAULT_MAXIMUM_CASES = 30;

  @RuleProperty(
    key = "maximum",
    description = "Maximum number of case",
    defaultValue = "" + DEFAULT_MAXIMUM_CASES)
  public int maximumCases = DEFAULT_MAXIMUM_CASES;


  public SwitchWithTooManyCaseCheck() {
    super(UastNode.Kind.SWITCH);
  }

  @Override
  public void visitNode(UastNode node) {
    SwitchLike switchLike = SwitchLike.from(node);
    int casesNb = switchLike.caseNodes().size();
    if(casesNb > maximumCases) {
      Issue.Message[] secondaries = switchLike.caseNodes().stream().map(cn -> new Issue.Message(cn.getChild(UastNode.Kind.KEYWORD).orElse(cn), "+1")).toArray(Issue.Message[]::new);
      reportIssue(switchLike.switchKeyword(), String.format("Reduce the number of switch cases from %d to at most %d.", casesNb, maximumCases), secondaries);
    }
  }

}
