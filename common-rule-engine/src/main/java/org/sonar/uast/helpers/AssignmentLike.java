package org.sonar.uast.helpers;

import org.sonar.uast.UastNode;

public class AssignmentLike {

  private final UastNode node;

  public AssignmentLike(UastNode node) {
    this.node = node;
  }

  public UastNode target() {
    return node.getChild(UastNode.Kind.ASSIGNMENT_TARGET).orElseThrow(IllegalStateException::new);
  }

  public UastNode value() {
    return node.getChild(UastNode.Kind.ASSIGNMENT_VALUE).orElseThrow(IllegalStateException::new);
  }
}
