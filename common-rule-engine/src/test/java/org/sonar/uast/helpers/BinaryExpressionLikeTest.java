package org.sonar.uast.helpers;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import org.junit.jupiter.api.Test;
import org.sonar.uast.UastNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class BinaryExpressionLikeTest {

  @Test
  void test() {
    UastNode leftOperand = node(UastNode.Kind.IDENTIFIER);
    UastNode operator = new UastNode(Collections.emptySet(), "", new UastNode.Token(42, 7, "+"), Collections.emptyList());
    UastNode rightOperand = node(UastNode.Kind.IDENTIFIER);
    UastNode binaryExpression = node(UastNode.Kind.BINARY_EXPRESSION, leftOperand, operator, rightOperand);

    BinaryExpressionLike assignmentLike = BinaryExpressionLike.from(binaryExpression);
    assertEquals(assignmentLike.leftOperand(), leftOperand);
    assertEquals(assignmentLike.operator(), operator);
    assertEquals(assignmentLike.rightOperand(), rightOperand);
  }

  @Test
  void test_not_binary_expression() {
    assertNull(BinaryExpressionLike.from(node(UastNode.Kind.CLASS)));
  }

  @Test
  void test_malformed() {
    assertNull(BinaryExpressionLike.from(node(UastNode.Kind.BINARY_EXPRESSION)));
  }

  private UastNode node(UastNode.Kind kind, UastNode... children) {
    return new UastNode(
      EnumSet.of(kind),
      "",
      null,
      Arrays.asList(children));
  }

}
