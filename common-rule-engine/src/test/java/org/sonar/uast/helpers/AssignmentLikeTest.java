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
package org.sonar.uast.helpers;

import java.util.Arrays;
import java.util.EnumSet;
import org.junit.jupiter.api.Test;
import org.sonar.uast.UastNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AssignmentLikeTest {

  @Test
  void test() {
    UastNode target = node(UastNode.Kind.ASSIGNMENT_TARGET);
    UastNode value = node(UastNode.Kind.ASSIGNMENT_VALUE);
    UastNode assignment = node(UastNode.Kind.ASSIGNMENT, target, value);

    AssignmentLike assignmentLike = AssignmentLike.from(assignment);
    assertEquals(assignmentLike.node(), assignment);
    assertEquals(assignmentLike.target(), target);
    assertEquals(assignmentLike.value(), value);
  }

  @Test
  void test_not_assignment() {
    assertNull(AssignmentLike.from(node(UastNode.Kind.CLASS)));
  }

  @Test
  void test_malformed() {
    assertNull(AssignmentLike.from(node(UastNode.Kind.ASSIGNMENT)));
  }

  private UastNode node(UastNode.Kind kind, UastNode... children) {
    return new UastNode(
      EnumSet.of(kind),
      "",
      null,
      Arrays.asList(children));
  }
}
