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

class SwitchLikeTest {

  @Test
  void test() throws Exception {
    UastNode node = UastNode.from(new StringReader("{ kinds: ['SWITCH'] }"));
    SwitchLike switchLike = SwitchLike.from(node);
    assertThat(switchLike).isNotNull();
    assertThat(switchLike.caseNodes()).isEmpty();

    node = UastNode.from(new StringReader("{ kinds: ['SWITCH'], children: [ { kinds: ['CASE'] }] }"));
    switchLike = SwitchLike.from(node);
    assertThat(switchLike).isNotNull();
    assertThat(switchLike.caseNodes()).hasSize(1);
  }
}
