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
import static org.sonar.uast.validators.ValidatorTestUtils.keyword;
import static org.sonar.uast.validators.ValidatorTestUtils.node;

class IfValidatorTest {
  private static Engine engine;

  @BeforeAll
  static void setUp() {
    engine = new Engine(Collections.emptyList(), Collections.singleton(new IfValidator()));
  }

  private static void validate(UastNode node) throws IOException {
    engine.scan(node, null);
  }

  @Test
  void expected() {
    UastNode ifNode = buildNode(Kind.IF).addChildren(keyword("if", Kind.IF_KEYWORD), node(Kind.CONDITION), node(Kind.THEN)).build();
    UastNode ifElseNode = buildNode(Kind.IF).addChildren(keyword("if", Kind.IF_KEYWORD), node(Kind.CONDITION), node(Kind.THEN),keyword("if", Kind.ELSE_KEYWORD),  node(Kind.ELSE)).build();
    try {
      validate(ifNode);
      validate(ifElseNode);
    } catch (Exception e) {
      fail("should not have failed", e);
    }
  }

  @Test
  void missing_if_keyword() {
    UastNode ifNode = node(Kind.IF, keyword("label"), node(Kind.THEN));
    Validator.ValidationException exception = assertThrows(Validator.ValidationException.class, () -> validate(ifNode));
    assertThat(exception.getMessage()).isEqualTo("IfValidator at line 1: Expected 'if' as keyword but got 'label'.");
  }

  @Test
  void missing_else_keyword() {
    UastNode ifNode = node(Kind.IF, keyword("if", Kind.IF_KEYWORD), node(Kind.CONDITION), node(Kind.THEN), node(Kind.ELSE));
    Validator.ValidationException exception = assertThrows(Validator.ValidationException.class, () -> validate(ifNode));
    assertThat(exception.getMessage()).isEqualTo("IfValidator at line 1: Should have one single child of kind 'ELSE_KEYWORD' but got none.");
  }
}
