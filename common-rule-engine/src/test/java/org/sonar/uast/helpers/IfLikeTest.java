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

import static org.assertj.core.api.Assertions.assertThat;

class IfLikeTest {

  @Test
  void must_have_condition() throws Exception {
    UastNode node = UastNode.from(new StringReader("{ kinds: ['IF'], children: ["
      + "{kinds: ['IF_KEYWORD'], token: {value: 'if', line: 1, column: 1}},"
      + "{kinds: ['CONDITION'], token: {value: 'cond', line: 1, column: 1}},"
      + "{kinds: ['THEN'], token: {value: 'statement1', line: 1, column: 1}}"
      + "] }"));
    IfLike ifLike = IfLike.from(node);
    assertThat(ifLike).isNotNull();
    assertThat(ifLike.ifKeyword().joinTokens()).isEqualTo("if");
    assertThat(ifLike.condition().joinTokens()).isEqualTo("cond");
    assertThat(ifLike.thenNode().joinTokens()).isEqualTo("statement1");

    node = UastNode.from(new StringReader("{ kinds: ['IF'] }"));
    assertThat(IfLike.from(node)).isNull();
  }

  @Test
  void has_else() throws  Exception {
    UastNode node = UastNode.from(new StringReader("{ kinds: ['IF'], " +
      "children: [" +
        "{kinds: ['IF_KEYWORD'], token: {value: 'if', line: 1, column: 1}}," +
        "{kinds: ['CONDITION'], token: {value: 'cond', line: 1, column: 1}}," +
        "{kinds: ['THEN'], token: {value: 'statement1', line: 1, column: 1}}," +
        "{kinds: ['ELSE_KEYWORD'], token: {value: 'else', line: 1, column: 1}}," +
        "{kinds: ['ELSE'], token: {value: 'statement2', line: 1, column: 1}}" +
      "] }"));
    IfLike ifLike = IfLike.from(node);
    assertThat(ifLike).isNotNull();
    IfLike.ElseLike elseLike = ifLike.elseLike();
    assertThat(elseLike).isNotNull();
    assertThat(elseLike.elseKeyword().joinTokens()).isEqualTo("else");
    assertThat(elseLike.elseNode().joinTokens()).isEqualTo("statement2");
  }
}
