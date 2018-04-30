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
import org.sonar.uast.UastNode.Kind;
import org.sonar.uast.helpers.BinaryExpressionLike;

@Rule(key = "S3981")
public class CollectionSizeCheck extends Check {

  private static final String MESSAGE = "The length of a collection is always \">=0\", so update this test to either \"==0\" or \">0\".";

  public CollectionSizeCheck() {
    super(Kind.GREATER_OR_EQUAL, Kind.LESS_THAN);
  }

  @Override
  public void visitNode(UastNode node) {
    BinaryExpressionLike binaryExpression = BinaryExpressionLike.from(node);
    if (binaryExpression != null) {
      UastNode rhs = binaryExpression.rightOperand();
      UastNode lhs = binaryExpression.leftOperand();
      if (isLenCall(lhs) && isZero(rhs)) {
        reportIssue(node, MESSAGE);
      }
    }
  }

  private static boolean isZero(UastNode node) {
    return node.is(Kind.INT_LITERAL) && node.token != null && node.token.value.equals("0");
  }

  private static boolean isLenCall(UastNode node) {
    if (node.is(Kind.CALL)) {
      UastNode callee = node.getChildren(Kind.EXPRESSION).get(0);
      return callee.is(Kind.IDENTIFIER) && callee.token != null && callee.token.value.equals("len");
    }
    return false;
  }
}
