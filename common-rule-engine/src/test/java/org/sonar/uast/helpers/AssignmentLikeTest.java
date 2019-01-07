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

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.uast.UastNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AssignmentLikeTest {

  @Test
  void test() {
    UastNode target = node(UastNode.Kind.ASSIGNMENT_TARGET);
    UastNode operator = node(UastNode.Kind.ASSIGNMENT_OPERATOR);
    UastNode value = node(UastNode.Kind.ASSIGNMENT_VALUE);
    UastNode assignment = node(UastNode.Kind.ASSIGNMENT, target, operator, value);

    AssignmentLike assignmentLike = AssignmentLike.from(assignment);
    assertEquals(assignmentLike.node(), assignment);
    assertEquals(assignmentLike.target(), target);
    assertEquals(assignmentLike.operator(), operator);
    assertEquals(assignmentLike.value(), value);
    assertFalse(assignmentLike.isMultiple());

    List<AssignmentLike> assignmentsTuples = assignmentLike.assignmentsTuples();
    assertEquals(assignmentsTuples.size(), 1);
    assertEquals(assignmentsTuples.get(0), assignmentLike);
    // same instance, not recomputed
    assertTrue(assignmentLike.assignmentsTuples() == assignmentsTuples);
  }

  @Test
  void test_multipe() {
    UastNode t1 = node(UastNode.Kind.ASSIGNMENT_TARGET);
    UastNode t2 = node(UastNode.Kind.ASSIGNMENT_TARGET);
    UastNode target = node(UastNode.Kind.ASSIGNMENT_TARGET_LIST, t1, t2);
    UastNode operator = node(UastNode.Kind.ASSIGNMENT_OPERATOR);
    UastNode v1 = node(UastNode.Kind.ASSIGNMENT_VALUE);
    UastNode v2 = node(UastNode.Kind.ASSIGNMENT_VALUE);
    UastNode value = node(UastNode.Kind.ASSIGNMENT_VALUE_LIST, v1, v2);
    UastNode assignment = node(UastNode.Kind.ASSIGNMENT, target, operator, value);

    AssignmentLike assignmentLike = AssignmentLike.from(assignment);
    assertEquals(assignmentLike.node(), assignment);
    assertEquals(assignmentLike.target(), target);
    assertEquals(assignmentLike.operator(), operator);
    assertEquals(assignmentLike.value(), value);
    assertTrue(assignmentLike.isMultiple());

    List<AssignmentLike> assignmentsTuples = assignmentLike.assignmentsTuples();
    assertFalse(assignmentsTuples.isEmpty());
    assertEquals(assignmentsTuples.size(), 2);
    // same instance, not recomputed
    assertTrue(assignmentLike.assignmentsTuples() == assignmentsTuples);

    AssignmentLike childAssignment1 = assignmentsTuples.get(0);
    // shared with parent multiple assignment
    assertEquals(childAssignment1.node(), assignment);
    assertEquals(childAssignment1.operator(), operator);
    // assignment is a single-assignment
    assertEquals(childAssignment1.target(), t1);
    assertEquals(childAssignment1.value(), v1);
    assertFalse(childAssignment1.isMultiple());

    AssignmentLike childAssignment2 = assignmentsTuples.get(1);
    // shared with parent multiple assignment
    assertEquals(childAssignment2.node(), assignment);
    assertEquals(childAssignment2.operator(), operator);
    // assignment is a single-assignment
    assertEquals(childAssignment2.target(), t2);
    assertEquals(childAssignment2.value(), v2);
    assertFalse(childAssignment2.isMultiple());
  }

  @Test
  void test_multipe_malformed() {
    UastNode target = node(UastNode.Kind.ASSIGNMENT_TARGET_LIST);
    UastNode operator = node(UastNode.Kind.ASSIGNMENT_OPERATOR);
    UastNode value = node(UastNode.Kind.ASSIGNMENT_VALUE_LIST, node(UastNode.Kind.ASSIGNMENT_VALUE));
    UastNode assignment = node(UastNode.Kind.ASSIGNMENT, target, operator, value);

    AssignmentLike assignmentLike = AssignmentLike.from(assignment);
    assertEquals(assignmentLike.node(), assignment);
    assertEquals(assignmentLike.target(), target);
    assertEquals(assignmentLike.operator(), operator);
    assertEquals(assignmentLike.value(), value);
    assertTrue(assignmentLike.isMultiple());

    List<AssignmentLike> assignmentsTuples = assignmentLike.assignmentsTuples();
    // not the same number of target and values
    assertTrue(assignmentsTuples.isEmpty());
    // same instance, not recomputed
    assertTrue(assignmentLike.assignmentsTuples() == assignmentsTuples);
  }

  @Test
  void test_not_assignment() {
    assertNull(AssignmentLike.from(node(UastNode.Kind.CLASS)));
  }

  @Test
  void test_malformed() {
    UastNode target = node(UastNode.Kind.ASSIGNMENT_TARGET);
    UastNode operator = node(UastNode.Kind.ASSIGNMENT_OPERATOR);
    UastNode value = node(UastNode.Kind.ASSIGNMENT_VALUE);

    assertNull(AssignmentLike.from(node(UastNode.Kind.ASSIGNMENT)));
    assertNull(AssignmentLike.from(node(UastNode.Kind.ASSIGNMENT, target, value)));
    assertNull(AssignmentLike.from(node(UastNode.Kind.ASSIGNMENT, target, operator)));
    assertNull(AssignmentLike.from(node(UastNode.Kind.ASSIGNMENT, operator, value)));

    UastNode targetList = node(UastNode.Kind.ASSIGNMENT_TARGET_LIST);
    UastNode valueList = node(UastNode.Kind.ASSIGNMENT_VALUE_LIST);
    assertNull(AssignmentLike.from(node(UastNode.Kind.ASSIGNMENT, target, operator, valueList)));
    assertNull(AssignmentLike.from(node(UastNode.Kind.ASSIGNMENT, targetList, operator, value)));
  }

  private UastNode node(UastNode.Kind kind, UastNode... children) {
    return new UastNode(
      EnumSet.of(kind),
      "",
      null,
      Arrays.asList(children));
  }
}
