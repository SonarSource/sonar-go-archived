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

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.commonruleengine.checks.TestUtils;
import org.sonar.uast.UastNode;

import static org.assertj.core.api.Assertions.assertThat;

class ArrayAccessLikeTest {

  @Test
  void test_go() throws IOException {
    UastNode uast = TestUtils.goUast(Paths.get("src","test","files","kinds","ArrayAccess.go"));
    List<UastNode> arrayAccess = new ArrayList<>();
    uast.getDescendants(UastNode.Kind.ARRAY_ACCESS_EXPRESSION, arrayAccess::add);
    assertThat(arrayAccess).hasSize(1);
    ArrayAccessLike arrayAccessLike = ArrayAccessLike.from(arrayAccess.get(0));
    assertThat(arrayAccessLike).isNotNull();
    assertThat(arrayAccessLike.node().joinTokens()).isEqualTo("a.field[i + 1]");
    assertThat(arrayAccessLike.objectExpression().joinTokens()).isEqualTo("a.field");
    assertThat(arrayAccessLike.keyExpression().joinTokens()).isEqualTo("i + 1");
  }

}
