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
import org.sonar.uast.helpers.AssignmentLike;

@Rule(key = "S2757")
public class WrongAssignmentOperatorCheck extends Check {

  public WrongAssignmentOperatorCheck() {
    super(UastNode.Kind.ASSIGNMENT);
  }

  @Override
  public void visitNode(UastNode node) {
    AssignmentLike assignmentLike = AssignmentLike.from(node);
    if (assignmentLike == null) {
      return;
    }
    if (assignmentLike.isMultiple()) {
      List<AssignmentLike> couples = assignmentLike.assignmentsTuples();
      if (couples.size() != 1) {
        // not relevant for multiple assignment form
        return;
      }
      assignmentLike = couples.get(0);
    }

    UastNode.Token variableLastToken = assignmentLike.target().lastToken();
    UastNode operator = assignmentLike.operator();
    UastNode expression = assignmentLike.value();
    UastNode.Token expressionFirstToken = expression.firstToken();

    if (noSpacingBetween(operator.lastToken(), expressionFirstToken)
      && spacingBetween(variableLastToken, operator.firstToken())
      && expression.is(UastNode.Kind.UNARY_MINUS, UastNode.Kind.UNARY_PLUS, UastNode.Kind.LOGICAL_COMPLEMENT)) {
      String msg;
      if (expression.is(UastNode.Kind.LOGICAL_COMPLEMENT)) {
        msg = "Add a space between \"=\" and \"!\" to avoid confusion.";
      } else {
        msg = String.format("Was \"%s=\" meant instead?", expressionFirstToken.value);
      }
      reportIssue(assignmentLike.node(), msg);
    }
  }

  private static boolean spacingBetween(UastNode.Token firstToken, UastNode.Token secondToken) {
    return !noSpacingBetween(firstToken, secondToken);
  }

  private static boolean noSpacingBetween(UastNode.Token firstToken, UastNode.Token secondToken) {
    return firstToken.line == secondToken.line && firstToken.endColumn + 1 == secondToken.column;
  }

}
