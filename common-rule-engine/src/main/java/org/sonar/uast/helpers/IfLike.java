package org.sonar.uast.helpers;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.uast.UastNode;

public class IfLike {

  private final UastNode node;

  public IfLike(UastNode node) {
    this.node = node;
  }

  @CheckForNull
  public static IfLike from(@Nullable UastNode node) {
    if (node == null) {
      return null;
    }
    if (node.kinds.contains(UastNode.Kind.IF)) {
      return new IfLike(node);
    }
    return null;
  }

  public UastNode node() {
    return node;
  }

  public UastNode condition() {
    return node.getChild(UastNode.Kind.CONDITION).orElse(null);
  }

  @CheckForNull
  public UastNode elseNode() {
    return node.getChild(UastNode.Kind.ELSE).orElse(null);
  }

}
