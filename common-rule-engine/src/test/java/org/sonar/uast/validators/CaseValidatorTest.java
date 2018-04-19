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
import static org.sonar.uast.validators.ValidatorTestUtils.token;

public class CaseValidatorTest {

  private static Engine engine;

  @BeforeAll
  public static void setUp() {
    engine = new Engine(Collections.emptyList(), Collections.singleton(new CaseValidator()));
  }

  private static void validate(UastNode node) throws IOException {
    engine.scan(node, null);
  }

  @Test
  public void single_condition() throws Exception {
    UastNode caseNode = node(UastNode.Kind.CASE,
      keyword("case"),
      node(UastNode.Kind.CONDITION),
      token(":"));
    UastNode switchNode = node(UastNode.Kind.SWITCH, caseNode);

    try {
      validate(switchNode);
    } catch (Exception e) {
      fail("should not have failed", e);
    }
  }

  @Test
  public void multiple_conditions() throws Exception {
    UastNode caseNode = node(UastNode.Kind.CASE,
      keyword("case"),
      node(UastNode.Kind.CONDITION),
      token(","),
      node(UastNode.Kind.CONDITION),
      token(","),
      node(UastNode.Kind.CONDITION),
      token(":"));
    UastNode switchNode = node(UastNode.Kind.SWITCH, caseNode);

    try {
      validate(switchNode);
    } catch (Exception e) {
      fail("should not have failed", e);
    }
  }

  @Test
  public void ancestor_not_direct_parent() throws Exception {
    UastNode caseNode = node(UastNode.Kind.CASE,
      keyword("case"),
      node(UastNode.Kind.CONDITION),
      token(":"));
    UastNode parent = node(UastNode.Kind.UNSUPPORTED, caseNode);
    UastNode switchNode = node(UastNode.Kind.SWITCH, parent);

    try {
      validate(switchNode);
    } catch (Exception e) {
      fail("should not have failed", e);
    }
  }

  @Test
  public void wrong_keyword() throws Exception {
    UastNode caseNode = node(UastNode.Kind.CASE,
      keyword("default"),
      node(UastNode.Kind.CONDITION),
      token(":"));
    UastNode switchNode = node(UastNode.Kind.SWITCH, caseNode);

    Validator.ValidationException exception = assertThrows(Validator.ValidationException.class, () -> validate(switchNode));
    assertThat(exception.getMessage()).isEqualTo("CaseValidator: Expected 'case' as keyword but got 'default'.");
  }

  @Test
  public void not_part_of_switch() throws Exception {
    UastNode caseNode = node(UastNode.Kind.CASE,
      keyword("case"),
      node(UastNode.Kind.CONDITION),
      token(":"));
    UastNode parent = node(UastNode.Kind.ADD, caseNode);

    Validator.ValidationException exception = assertThrows(Validator.ValidationException.class, () -> validate(parent));
    assertThat(exception.getMessage()).isEqualTo("CaseValidator: Should have a node of kind 'SWITCH' as ancestor.");
  }

  @Test
  public void should_have_condition() throws Exception {
    UastNode caseNode = node(UastNode.Kind.CASE,
      keyword("case"),
      token(":"));
    UastNode switchNode = node(UastNode.Kind.SWITCH, caseNode);

    Validator.ValidationException exception = assertThrows(Validator.ValidationException.class, () -> validate(switchNode));
    assertThat(exception.getMessage()).isEqualTo("CaseValidator: Should have at least one child of kind 'CONDITION'.");
  }

}
