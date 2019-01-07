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
import static org.sonar.uast.validators.ValidatorTestUtils.NodeBuilder.buildNode;
import static org.sonar.uast.validators.ValidatorTestUtils.node;

class ParenthesizedExpressionValidatorTest {
  private static Engine engine;

  @BeforeAll
  static void setUp() {
    engine = new Engine(Collections.emptyList(), Collections.singleton(new ParenthesizedExpressionValidator()));
  }

  private static void validate(UastNode node) throws IOException {
    engine.scan(node, null);
  }

  @Test
  void expected() {
    UastNode parenthesisNode = buildNode(Kind.PARENTHESIZED_EXPRESSION).addChildren(node(Kind.LEFT_PARENTHESIS), node(Kind.EXPRESSION), node(Kind.RIGHT_PARENTHESIS)).build();
    try {
      validate(parenthesisNode);
    } catch (Exception e) {
      fail("should not have failed", e);
    }
  }

  @Test
  void missing_left_right_parenth_or_expression() {
    UastNode parenthesisNode0 = buildNode(Kind.PARENTHESIZED_EXPRESSION).addChildren(node(Kind.EXPRESSION), node(Kind.RIGHT_PARENTHESIS)).build();
    Validator.ValidationException exception = assertThrows(Validator.ValidationException.class, () -> validate(parenthesisNode0));
    assertThat(exception.getMessage()).isEqualTo("ParenthesizedExpressionValidator at line N/A: Should have one single child of kind 'LEFT_PARENTHESIS' but got none.");

    UastNode parenthesisNode1 = buildNode(Kind.PARENTHESIZED_EXPRESSION).addChildren(node(Kind.LEFT_PARENTHESIS), node(Kind.EXPRESSION)).build();
    exception = assertThrows(Validator.ValidationException.class, () -> validate(parenthesisNode1));
    assertThat(exception.getMessage()).isEqualTo("ParenthesizedExpressionValidator at line N/A: Should have one single child of kind 'RIGHT_PARENTHESIS' but got none.");

    UastNode parenthesisNode2 = buildNode(Kind.PARENTHESIZED_EXPRESSION).addChildren(node(Kind.LEFT_PARENTHESIS), node(Kind.RIGHT_PARENTHESIS)).build();
    exception = assertThrows(Validator.ValidationException.class, () -> validate(parenthesisNode2));
    assertThat(exception.getMessage()).isEqualTo("ParenthesizedExpressionValidator at line N/A: Should have one single child of kind 'EXPRESSION' but got none.");

  }
}
