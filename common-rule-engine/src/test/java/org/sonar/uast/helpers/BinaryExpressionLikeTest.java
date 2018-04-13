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
package org.sonar.uast.helpers;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import org.junit.jupiter.api.Test;
import org.sonar.uast.UastNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class BinaryExpressionLikeTest {

  @Test
  void test() {
    UastNode leftOperand = node(UastNode.Kind.LEFT_OPERAND);
    UastNode operator = new UastNode(Collections.singleton(UastNode.Kind.OPERATOR), "",
      new UastNode.Token(42, 7, "+"), Collections.emptyList());
    UastNode rightOperand = node(UastNode.Kind.RIGHT_OPERAND);
    UastNode binaryExpression = node(UastNode.Kind.BINARY_EXPRESSION, leftOperand, operator, rightOperand);

    BinaryExpressionLike assignmentLike = BinaryExpressionLike.from(binaryExpression);
    assertEquals(assignmentLike.leftOperand(), leftOperand);
    assertEquals(assignmentLike.operator(), operator);
    assertEquals(assignmentLike.rightOperand(), rightOperand);
  }

  @Test
  void test_not_binary_expression() {
    assertNull(BinaryExpressionLike.from(node(UastNode.Kind.CLASS)));
  }

  @Test
  void test_malformed() {
    assertNull(BinaryExpressionLike.from(node(UastNode.Kind.BINARY_EXPRESSION)));
  }

  private UastNode node(UastNode.Kind kind, UastNode... children) {
    return new UastNode(
      EnumSet.of(kind),
      "",
      null,
      Arrays.asList(children));
  }

}
