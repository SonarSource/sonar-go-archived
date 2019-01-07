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
import org.sonar.uast.UastNode;
import org.sonar.uast.helpers.SwitchLike;

/**
 * https://jira.sonarsource.com/browse/RSPEC-131
 */
@Rule(key = "S131")
public class SwitchWithoutDefaultCheck extends Check {

  public SwitchWithoutDefaultCheck() {
    super(UastNode.Kind.SWITCH);
  }

  @Override
  public void visitNode(UastNode node) {
    SwitchLike switchNode = SwitchLike.from(node);
    if (switchNode.caseNodes().stream().noneMatch(UastNode.Kind.DEFAULT_CASE)) {
      reportIssue(switchNode.switchKeyword(), "Add a default case to this switch.");
    }
  }

}
