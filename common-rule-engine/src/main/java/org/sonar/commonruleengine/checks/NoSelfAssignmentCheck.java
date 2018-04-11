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
import org.sonar.uast.UastNode;
import org.sonar.uast.helpers.AssignmentLike;

import static org.sonar.uast.Uast.syntacticallyEquivalent;

/**
 * https://jira.sonarsource.com/browse/RSPEC-1656
 */
@Rule(key = "S1656")
public class NoSelfAssignmentCheck extends Check {

  public NoSelfAssignmentCheck() {
    super(AssignmentLike.KIND);
  }

  @Override
  public void visitNode(UastNode node) {
    AssignmentLike assignment = AssignmentLike.from(node);
    if (assignment != null
      && node.isNot(UastNode.Kind.VARIABLE_DECLARATION, UastNode.Kind.COMPOUND_ASSIGNMENT)
      && syntacticallyEquivalent(assignment.target(), assignment.value())) {
      reportIssue(node, "Remove this self assignment");
    }
  }
}
