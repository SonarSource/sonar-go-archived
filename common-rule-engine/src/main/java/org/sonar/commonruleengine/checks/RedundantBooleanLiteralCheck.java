package org.sonar.commonruleengine.checks;

import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.uast.UastNode;
import org.sonar.uast.helpers.BinaryExpressionLike;

/**
 * https://jira.sonarsource.com/browse/RSPEC-1125
 */
@Rule(key = "S1125")
public class RedundantBooleanLiteralCheck extends Check {

  public RedundantBooleanLiteralCheck() {
    super(UastNode.Kind.BINARY_EXPRESSION);
  }

  @Override
  public void visitNode(UastNode node) {
    BinaryExpressionLike binExpr = BinaryExpressionLike.from(node);
    if (binExpr == null) {
      return;
    }
    Optional<UastNode> booleanLiteralOperand = findBooleanLiteralOperand(binExpr);
    if (binExpr.operator().is(UastNode.Kind.OPERATOR_EQUAL, UastNode.Kind.OPERATOR_NOT_EQUAL, UastNode.Kind.OPERATOR_LOGICAL_AND, UastNode.Kind.OPERATOR_LOGICAL_OR)
      && booleanLiteralOperand.isPresent()) {
      reportIssue(booleanLiteralOperand.get(), "Remove this redundant boolean literal");
    }
  }

  private static Optional<UastNode> findBooleanLiteralOperand(BinaryExpressionLike binExpr) {
    if (binExpr.rightOperand().is(UastNode.Kind.BOOLEAN_LITERAL)) {
      return Optional.of(binExpr.rightOperand());
    }
    if (binExpr.leftOperand().is(UastNode.Kind.BOOLEAN_LITERAL)) {
      return Optional.of(binExpr.leftOperand());
    }
    return Optional.empty();
  }
}
