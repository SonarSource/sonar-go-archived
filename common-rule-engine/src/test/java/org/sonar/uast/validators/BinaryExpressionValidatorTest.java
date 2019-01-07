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
package org.sonar.uast.validators;

import java.io.IOException;
import java.util.Collections;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sonar.commonruleengine.Engine;
import org.sonar.uast.UastNode;
import org.sonar.uast.UastNode.Kind;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.sonar.uast.validators.ValidatorTestUtils.node;

class BinaryExpressionValidatorTest {
  private static Engine engine;

  @BeforeAll
  static void setUp() {
    engine = new Engine(Collections.emptyList(), Collections.singleton(new BinaryExpressionValidator()));
  }

  private static void validate(UastNode node) throws IOException {
    engine.scan(node, null);
  }

  @Test
  void expected() {
    UastNode binaryOperator = node(Kind.BINARY_EXPRESSION, node(Kind.LEFT_OPERAND), node(Kind.OPERATOR), node(Kind.RIGHT_OPERAND));
    try {
      validate(binaryOperator);
    } catch (Exception e) {
      fail("should not have failed", e);
    }
  }

  @Test
  void missing_left_or_right_operand_or_operator() {
    UastNode binaryOperator0 = node(Kind.BINARY_EXPRESSION, node(Kind.OPERATOR), node(Kind.RIGHT_OPERAND));
    Validator.ValidationException exception = assertThrows(Validator.ValidationException.class, () -> validate(binaryOperator0));
    assertThat(exception.getMessage()).isEqualTo("BinaryExpressionValidator at line N/A: Should have one single child of kind 'LEFT_OPERAND' but got none.");

    UastNode binaryOperator1 = node(Kind.BINARY_EXPRESSION, node(Kind.LEFT_OPERAND), node(Kind.OPERATOR));
    exception = assertThrows(Validator.ValidationException.class, () -> validate(binaryOperator1));
    assertThat(exception.getMessage()).isEqualTo("BinaryExpressionValidator at line N/A: Should have one single child of kind 'RIGHT_OPERAND' but got none.");

    UastNode binaryOperator2 = node(Kind.BINARY_EXPRESSION, node(Kind.LEFT_OPERAND), node(Kind.RIGHT_OPERAND));
    exception = assertThrows(Validator.ValidationException.class, () -> validate(binaryOperator2));
    assertThat(exception.getMessage()).isEqualTo("BinaryExpressionValidator at line N/A: Should have one single child of kind 'OPERATOR' but got none.");
  }
}
