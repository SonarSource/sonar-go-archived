package org.sonar.uast.helpers;

import java.util.Optional;
import org.sonar.uast.UastNode;

public class ParenthesizedLike {

  private final UastNode node;
  private final UastNode expression;


  private ParenthesizedLike(UastNode node, UastNode expression) {
    this.node = node;
    this.expression = expression;
  }

  public static ParenthesizedLike from(UastNode node) {
    if (node.kinds.contains(UastNode.Kind.PARENTHESIZED_EXPRESSION)) {
      Optional<UastNode> expression = node.getChild(UastNode.Kind.EXPRESSION);
      if (expression.isPresent()) {
        return new ParenthesizedLike(node, expression.get());
      }
    }
    return null;
  }

  public UastNode node() {
    return node;
  }

  public UastNode expression() {
    return expression;
  }
}
