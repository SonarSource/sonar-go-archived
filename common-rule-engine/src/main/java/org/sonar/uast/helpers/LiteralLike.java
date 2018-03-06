package org.sonar.uast.helpers;

import java.util.Optional;
import org.sonar.uast.UastNode;

public class LiteralLike {

  private final UastNode node;

  private LiteralLike(UastNode node) {
    this.node = node;
  }

  public static LiteralLike from(UastNode node) {
    if (node.kinds.contains(UastNode.Kind.LITERAL)) {
      return new LiteralLike(node);
    }
    if (node.children.size() == 1) {
      Optional<UastNode> childLiteral = node.getChild(UastNode.Kind.LITERAL);
      if (childLiteral.isPresent()) {
        return new LiteralLike(childLiteral.get());
      }
    }
    return null;
  }

  public String value() {
    return node.joinTokens();
  }

}
