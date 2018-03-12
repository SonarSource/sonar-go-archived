package org.sonar.uast.helpers;

import java.util.Optional;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.uast.UastNode;

public class IfLike {

  private final UastNode node;
  private final UastNode condition;
  private final UastNode elseNode;

  public IfLike(UastNode node, UastNode condition, @Nullable UastNode elseNode) {
    this.node = node;
    this.condition = condition;
    this.elseNode = elseNode;
  }

  @CheckForNull
  public static IfLike from(@Nullable UastNode node) {
    if (node == null) {
      return null;
    }
    if (node.kinds.contains(UastNode.Kind.IF)) {
      Optional<UastNode> condition = node.getChild(UastNode.Kind.CONDITION);
      UastNode elseNode = node.getChild(UastNode.Kind.ELSE).orElse(null);
      if (condition.isPresent()) {
        return new IfLike(node, condition.get(), elseNode);
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
    return elseNode;
  }

}
