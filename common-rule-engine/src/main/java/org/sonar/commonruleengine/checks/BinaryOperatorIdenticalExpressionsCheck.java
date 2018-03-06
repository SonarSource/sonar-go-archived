package org.sonar.commonruleengine.checks;

import javax.annotation.CheckForNull;
import org.sonar.check.Rule;
import org.sonar.uast.UastNode;

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
    if (binaryExpression != null && syntacticallyEquivalent(binaryExpression.leftOperand, binaryExpression.rightOperand)) {
      reportIssue(node, "Correct one of the identical argument sub-expressions.");
    }
  }

  private static class BinaryExpressionLike {
    private UastNode rightOperand;
    private UastNode leftOperand;
    private UastNode.Token operatorToken;

    @CheckForNull
    public static BinaryExpressionLike from(UastNode node) {
      if (node.children.size() != 3) {
        // malformed binary operators?
        return null;
      }
      BinaryExpressionLike result = new BinaryExpressionLike();
      result.rightOperand = node.children.get(0);
      result.operatorToken = node.children.get(1).token;
      result.leftOperand = node.children.get(2);
      return result;
    }
  }
}
