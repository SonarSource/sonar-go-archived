package org.sonar.uast.helpers;

import javax.annotation.CheckForNull;
import org.sonar.uast.UastNode;

public class BinaryExpressionLike {
  private final UastNode leftOperand;
  private final UastNode operator;
  private final UastNode rightOperand;

  public BinaryExpressionLike(UastNode leftOperand, UastNode operator, UastNode rightOperand) {
    this.leftOperand = leftOperand;
    this.operator = operator;
    this.rightOperand = rightOperand;
  }

  @CheckForNull
  public static BinaryExpressionLike from(UastNode node) {
    if (node.children.size() != 3) {
      // malformed binary operators?
      return null;
    }
    return new BinaryExpressionLike(node.children.get(0),
      node.children.get(1),
      node.children.get(2));
  }

  public UastNode leftOperand() {
    return leftOperand;
  }

  public UastNode operator() {
    return operator;
  }

  public UastNode rightOperand() {
    return rightOperand;
  }
}
