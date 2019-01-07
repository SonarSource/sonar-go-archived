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
package org.sonar.uast.helpers;

import java.io.StringReader;
import org.junit.jupiter.api.Test;
import org.sonar.uast.UastNode;

import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

class LiteralLikeTest {

  @Test
  void test() throws Exception  {
    UastNode literal = UastNode.from(new StringReader("{ \"kinds\": [\"LITERAL\"], \"token\": {\"value\": \"foo\" , \"line\": 1, \"column\": 1} }"));
    LiteralLike literalLike = LiteralLike.from(literal);
    assertThat(literalLike).isNotNull();
    assertThat(literalLike.value()).isEqualTo("foo");

    UastNode node = new UastNode(emptySet(), "", null, singletonList(literal));
    literalLike = LiteralLike.from(node);
    assertThat(literalLike).isNotNull();
    assertThat(literalLike.value()).isEqualTo("foo");
  }

  @Test
  void literal_nested_as_only_child() throws Exception {
    UastNode literal = UastNode.from(new StringReader(
      "{ \"kinds\": [], " +
        "\"children\": [{ \"kinds\": [\"LITERAL\"], \"token\": {\"value\": \"foo\" , \"line\": 1, \"column\": 1 } }]" +
        "}"));

    LiteralLike literalLike = LiteralLike.from(literal);
    assertThat(literalLike).isNotNull();
    assertThat(literalLike.value()).isEqualTo("foo");
  }

  @Test
  void multiple_literal_children() throws Exception {
    UastNode literal = UastNode.from(new StringReader(
      "{ \"kinds\": [], " +
        "\"children\": [" +
        "{ \"kinds\": [\"LITERAL\"], \"token\": {\"value\": \"foo\"  , \"line\": 1, \"column\": 1} }," +
        "{ \"kinds\": [\"LITERAL\"], \"token\": {\"value\": \"bar\" , \"line\": 1, \"column\": 1 } }" +
        "]" +
        "}"));

    LiteralLike literalLike = LiteralLike.from(literal);
    assertThat(literalLike).isNull();
  }
}
