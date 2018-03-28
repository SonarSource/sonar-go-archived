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
import org.sonar.commonruleengine.Issue;
import org.sonar.uast.UastNode;
import org.sonar.uast.helpers.BinaryExpressionLike;

import static org.sonar.uast.Uast.syntacticallyEquivalent;

/**
 * https://jira.sonarsource.com/browse/RSPEC-1764
 */
@Rule(key = "S1764")
public class BinaryOperatorIdenticalExpressionsCheck extends Check {

  public BinaryOperatorIdenticalExpressionsCheck() {
    super(UastNode.Kind.BINARY_EXPRESSION);
  }

  @Override
  public void visitNode(UastNode node) {
    BinaryExpressionLike binaryExpression = BinaryExpressionLike.from(node);
    if (binaryExpression != null
      && !isExcluded(binaryExpression)
      && syntacticallyEquivalent(binaryExpression.leftOperand(), binaryExpression.rightOperand())) {
      String operator = binaryExpression.operator().joinTokens();
      reportIssue(binaryExpression.rightOperand(),
        "Correct one of the identical sub-expressions on both sides of operator \"" + operator + "\".",
        new Issue.Message(binaryExpression.leftOperand()));
    }
  }

  private static boolean isExcluded(BinaryExpressionLike binaryExpression) {
    // "=" not considered as binary operator for Java and Go, might be for other languages?
    return binaryExpression.operator().is(UastNode.Kind.ASSIGNMENT_OPERATOR, UastNode.Kind.OPERATOR_ADD, UastNode.Kind.OPERATOR_MULTIPLY, UastNode.Kind.OPERATOR_LEFT_SHIFT);
  }
}
