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
package org.sonar.uast.validators;

import java.io.IOException;
import java.util.Collections;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sonar.commonruleengine.Engine;
import org.sonar.uast.UastNode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    UastNode ifNode = node(UastNode.Kind.IF, keyword("if", UastNode.Kind.IF_KEYWORD), node(UastNode.Kind.CONDITION), node(UastNode.Kind.THEN));
    try {
      validate(ifNode);
    } catch (Exception e) {
      fail("should not have failed", e);
    }
  }

  @Test
  void missing_if_keyword() {
    UastNode ifNode = node(UastNode.Kind.IF, keyword("label"), node(UastNode.Kind.THEN));

    Validator.ValidationException exception = assertThrows(Validator.ValidationException.class, () -> validate(ifNode));
    assertThat(exception.getMessage()).isEqualTo("IfValidator: Expected 'if' as keyword but got 'label'.");
  }
}
