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

import java.util.List;
import org.sonar.check.Rule;
import org.sonar.uast.UastNode;
import org.sonar.uast.helpers.SwitchLike;

/**
 * https://jira.sonarsource.com/browse/RSPEC-4524
 */
@Rule(key = "S4524")
public class SwitchDefaultLocationCheck extends Check {

  public SwitchDefaultLocationCheck() {
    super(UastNode.Kind.SWITCH);
  }

  @Override
  public void visitNode(UastNode node) {
    List<UastNode> caseNodes = SwitchLike.from(node).caseNodes();
    caseNodes.stream().filter(UastNode.Kind.DEFAULT_CASE).findFirst().ifPresent(defaultCase -> {
      int index = caseNodes.indexOf(defaultCase);
      if (index != 0 && index != caseNodes.size() - 1) {
        reportIssue(defaultCase, "Move this \"default\" case clause to the beginning or end of this \"switch\" statement.");
      }
    });
  }

}
