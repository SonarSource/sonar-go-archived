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
    assertEquals(assignmentLike.assignment(), assignment);
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
    UastNode node = new UastNode();
    node.kinds = EnumSet.of(kind);
    node.children = Arrays.asList(children);
    return node;
  }
}
