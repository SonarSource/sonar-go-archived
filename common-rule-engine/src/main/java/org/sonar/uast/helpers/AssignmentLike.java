package org.sonar.uast.helpers;

import java.util.Optional;
import javax.annotation.CheckForNull;
import org.sonar.uast.UastNode;

public class AssignmentLike {

  private final UastNode node;
  private final UastNode target;
  private final UastNode value;

  private AssignmentLike(UastNode node, UastNode target, UastNode value) {
    this.node = node;
    this.target = target;
    this.value = value;
  }

  @CheckForNull
  public static AssignmentLike from(UastNode node) {
    if (node.kinds.contains(UastNode.Kind.ASSIGNMENT)) {
      Optional<UastNode> target = node.getChild(UastNode.Kind.ASSIGNMENT_TARGET);
      Optional<UastNode> value = node.getChild(UastNode.Kind.ASSIGNMENT_VALUE);
      if (target.isPresent() && value.isPresent()) {
        return new AssignmentLike(node, target.get(), value.get());
      }
    }
    return null;
  }

  public UastNode assignment() {
    return node;
  }

  public UastNode target() {
    return target;
  }

  public UastNode value() {
    return value;
  }
}
