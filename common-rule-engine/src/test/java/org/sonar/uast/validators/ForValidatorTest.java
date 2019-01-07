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
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sonar.commonruleengine.Engine;
import org.sonar.uast.UastNode;

import static org.sonar.uast.validators.ValidatorTestUtils.NodeBuilder.buildNode;
import static org.sonar.uast.validators.ValidatorTestUtils.keyword;
import static org.sonar.uast.validators.ValidatorTestUtils.node;

class ForValidatorTest {

  private static Engine engine;

  @BeforeAll
  static void setUp() {
    engine = new Engine(Collections.emptyList(), Collections.singleton(new ForValidator()));
  }

  private static void validate(UastNode node) throws IOException {
    engine.scan(node, null);
  }

  @Test
  void expected() throws Exception {
    UastNode forNode = buildNode(UastNode.Kind.FOR)
      .addChildren(keyword("for"), node(UastNode.Kind.FOR_INIT), node(UastNode.Kind.CONDITION), node(UastNode.Kind.FOR_UPDATE), node(UastNode.Kind.BODY))
      .build();
    validate(forNode);

    forNode = buildNode(UastNode.Kind.FOR)
      .addChildren(keyword("for"), node(UastNode.Kind.BODY))
      .build();
    validate(forNode);
  }

  @Test
  void missing_keyword() throws Exception {
    UastNode forNode = buildNode(UastNode.Kind.FOR)
      .addChildren(node(UastNode.Kind.BODY))
      .build();
    Assertions.assertThatThrownBy(() -> validate(forNode))
      .hasMessage("ForValidator at line N/A: No keyword found.");
  }

  @Test
  void missing_body() throws Exception {
    UastNode forNode = buildNode(UastNode.Kind.FOR)
      .addChildren(keyword("for"), node(UastNode.Kind.FOR_INIT), node(UastNode.Kind.CONDITION), node(UastNode.Kind.FOR_UPDATE))
      .build();
    Assertions.assertThatThrownBy(() -> validate(forNode))
      .hasMessage("ForValidator at line 1: Should have one single child of kind 'BODY' but got none.");
  }

  @Test
  void multiple_init() throws Exception {
    UastNode forNode = buildNode(UastNode.Kind.FOR)
      .addChildren(keyword("for"), node(UastNode.Kind.FOR_INIT), node(UastNode.Kind.FOR_INIT), node(UastNode.Kind.CONDITION), node(UastNode.Kind.FOR_UPDATE))
      .build();
    Assertions.assertThatThrownBy(() -> validate(forNode))
      .hasMessage("ForValidator at line 1: Should have one single child of kind 'FOR_INIT' but got [[FOR_INIT], [FOR_INIT]].");
  }

}
