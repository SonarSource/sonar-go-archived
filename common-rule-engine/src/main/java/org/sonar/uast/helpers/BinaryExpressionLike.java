package org.sonar.uast.helpers;

import javax.annotation.CheckForNull;
import org.sonar.uast.UastNode;

public class BinaryExpressionLike {
  private UastNode leftOperand;
  private UastNode.Token operatorToken;
  private UastNode rightOperand;

  @CheckForNull
  public static BinaryExpressionLike from(UastNode node) {
    if (node.children.size() != 3) {
      // malformed binary operators?
      return null;
    }
    BinaryExpressionLike result = new BinaryExpressionLike();
    result.leftOperand = node.children.get(0);
    result.operatorToken = node.children.get(1).token;
    result.rightOperand = node.children.get(2);
    return result;
  }

  public UastNode leftOperand() {
    return leftOperand;
  }

  public UastNode.Token operatorToken() {
    return operatorToken;
  }

  public UastNode rightOperand() {
    return rightOperand;
  }
}
