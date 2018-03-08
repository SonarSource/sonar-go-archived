package org.sonar.commonruleengine.checks;

import org.sonar.check.Rule;
import org.sonar.uast.UastNode;
import org.sonar.uast.helpers.BinaryExpressionLike;

import static org.sonar.uast.Uast.syntacticallyEquivalent;

/**
 * https://jira.sonarsource.com/browse/RSPEC-1764
 */
@Rule(key = "S1764")
public class BinaryOperatorIdenticalExpressionsCheck extends Check {

  public BinaryOperatorIdenticalExpressionsCheck() {
    super(UastNode.Kind.BINARY_EXPRESSION);
  }

  @Override
  public void visitNode(UastNode node) {
    BinaryExpressionLike binaryExpression = BinaryExpressionLike.from(node);
    if (binaryExpression != null
      && !isExcluded(binaryExpression)
      && syntacticallyEquivalent(binaryExpression.leftOperand(), binaryExpression.rightOperand())) {
      reportIssue(node, "Correct one of the identical argument sub-expressions.");
    }
  }

  private static boolean isExcluded(BinaryExpressionLike binaryExpression) {
    String operator = binaryExpression.operatorToken().value;
    // "=" not considered as binary operator for Java and Go, might be for other languages?
    return "+".equals(operator) || "*".equals(operator) || "=".equals(operator);
  }
}
