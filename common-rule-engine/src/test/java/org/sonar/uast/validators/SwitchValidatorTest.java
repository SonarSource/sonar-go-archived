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
import org.sonar.api.internal.google.common.collect.Sets;
import org.sonar.commonruleengine.Engine;
import org.sonar.uast.UastNode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.sonar.uast.validators.ValidatorTestUtils.keyword;
import static org.sonar.uast.validators.ValidatorTestUtils.node;

public class SwitchValidatorTest {

  private static Engine engine;

  @BeforeAll
  public static void setUp() {
    engine = new Engine(Collections.emptyList(), Collections.singleton(new SwitchValidator()));
  }

  private static void validate(UastNode node) throws IOException {
    engine.scan(node, null);
  }

  @Test
  public void expected() throws Exception {
    UastNode switchNode = node(Sets.newHashSet(UastNode.Kind.SWITCH),
      keyword("switch"),
      node(UastNode.Kind.EXPRESSION));

    try {
      validate(switchNode);
    } catch (Exception e) {
      fail("should not have failed", e);
    }
  }

  @Test
  public void do_not_have_switch_keyword() throws Exception {
    UastNode switchNode = node(Sets.newHashSet(UastNode.Kind.SWITCH, UastNode.Kind.STATEMENT),
      keyword("label"),
      node(UastNode.Kind.EXPRESSION));

    Validator.ValidationException exception = assertThrows(Validator.ValidationException.class, () -> validate(switchNode));
    assertThat(exception.getMessage()).isEqualTo("SwitchValidator: Expected 'switch' as keyword but got 'label'.");
  }

  @Test
  public void do_not_have_an_expression() throws Exception {
    UastNode switchNode = node(Sets.newHashSet(UastNode.Kind.SWITCH, UastNode.Kind.STATEMENT),
      keyword("switch"));

    Validator.ValidationException exception = assertThrows(Validator.ValidationException.class, () -> validate(switchNode));
    assertThat(exception.getMessage()).isEqualTo("SwitchValidator: Should have one single child of kind 'EXPRESSION' but got none.");
  }
}
