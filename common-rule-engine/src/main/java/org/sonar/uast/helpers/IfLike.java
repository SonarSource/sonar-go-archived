package org.sonar.uast.helpers;

import java.util.Optional;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.uast.UastNode;

public class IfLike {

  private final UastNode node;
  private final UastNode condition;

  public IfLike(UastNode node, UastNode condition) {
    this.node = node;
    this.condition = condition;
  }

  @CheckForNull
  public static IfLike from(@Nullable UastNode node) {
    if (node == null) {
      return null;
    }
    if (node.kinds.contains(UastNode.Kind.IF)) {
      Optional<UastNode> condition = node.getChild(UastNode.Kind.CONDITION);
      if (condition.isPresent()) {
        return new IfLike(node, condition.get());
      }
    }
    return null;
  }

  public UastNode node() {
    return node;
  }

  public UastNode condition() {
    return condition;
  }

  @CheckForNull
  public UastNode elseNode() {
    return node.getChild(UastNode.Kind.ELSE).orElse(null);
  }

}
