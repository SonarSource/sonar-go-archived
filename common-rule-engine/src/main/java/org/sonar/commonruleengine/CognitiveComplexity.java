package org.sonar.commonruleengine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import org.sonar.uast.UastNode;
import org.sonar.uast.helpers.BinaryExpressionLike;
import org.sonar.uast.helpers.IfLike;
import org.sonar.uast.helpers.ParenthesizedLike;

public class CognitiveComplexity {

  private boolean inAFunction;
  private int complexity;
  private int nesting;
  @Nullable
  private final List<Issue.Message> secondaryLocations;
  private final Set<UastNode> ignoredNode;

  public CognitiveComplexity(@Nullable List<Issue.Message> secondaryLocations) {
    complexity = 0;
    nesting = 1;
    inAFunction = false;
    ignoredNode = new HashSet<>();
    this.secondaryLocations = secondaryLocations;
  }

  public static CognitiveComplexity calculateFunctionComplexity(UastNode function) {
    CognitiveComplexity complexityVisitor = new CognitiveComplexity(new ArrayList<>());
    complexityVisitor.visit(null, function);
    return complexityVisitor;
  }

  private void visit(@Nullable UastNode parent, UastNode node) {
    // TODO function recursive call: increaseComplexityByOne
    // TODO 'goto, break, continue' to label: increaseComplexityByOne
    if (node.is(UastNode.Kind.FUNCTION)) {
      visitFunction(node);
    } else if (parent == null || !inAFunction || ignoredNode.contains(node)) {
      visitChildren(node);
    } else if (node.is(UastNode.Kind.ELSE)) {
      increaseComplexityByOne(keyword(parent, node));
      visitChildren(node);
    } else if (node.is(UastNode.Kind.IF, UastNode.Kind.SWITCH, UastNode.Kind.LOOP)) {
      increaseComplexityByNesting(keyword(parent, node));
      visitNestedChildren(node);
    } else if (node.is(UastNode.Kind.BINARY_EXPRESSION)) {
      visitBinaryExpression(node);
    } else if (node.is(UastNode.Kind.FUNCTION_LITERAL)) {
      visitNestedChildren(node);
    } else {
      visitChildren(node);
    }
  }

  private void visitFunction(UastNode node) {
    if (inAFunction) {
      visitNestedChildren(node);
    } else {
      inAFunction = true;
      visitChildren(node);
      inAFunction = false;
    }
  }

  private void visitBinaryExpression(UastNode node) {
    List<BinaryExpressionLike> expressionAsList = new ArrayList<>();
    flattenBinaryExpressions(node, expressionAsList);
    UastNode.Kind lastLogicalOperatorKind = null;
    for (BinaryExpressionLike binaryExpression : expressionAsList) {
      UastNode.Kind kind = logicalOperatorKind(binaryExpression);
      if (kind != null) {
        if (binaryExpression.node() != node) {
          ignoredNode.add(binaryExpression.node());
        }
        if (kind != lastLogicalOperatorKind) {
          increaseComplexityByOne(binaryExpression.operator());
        }
      }
      lastLogicalOperatorKind = kind;
    }
    visitChildren(node);
  }

  private static void flattenBinaryExpressions(UastNode node, List<BinaryExpressionLike> expressionAsList) {
    ParenthesizedLike parenthesizedNode = ParenthesizedLike.from(node);
    if (parenthesizedNode != null) {
      flattenBinaryExpressions(parenthesizedNode.expression(), expressionAsList);
      return;
    }
    BinaryExpressionLike binaryExpression = BinaryExpressionLike.from(node);
    if (binaryExpression != null && logicalOperatorKind(binaryExpression) != null) {
      flattenBinaryExpressions(binaryExpression.leftOperand(), expressionAsList);
      expressionAsList.add(binaryExpression);
      flattenBinaryExpressions(binaryExpression.rightOperand(), expressionAsList);
    }
  }

  @Nullable
  private static UastNode.Kind logicalOperatorKind(BinaryExpressionLike binaryExpression) {
    if (binaryExpression.operator().is(UastNode.Kind.OPERATOR_LOGICAL_AND)) {
      return UastNode.Kind.OPERATOR_LOGICAL_AND;
    } else if (binaryExpression.operator().is(UastNode.Kind.OPERATOR_LOGICAL_OR)) {
      return UastNode.Kind.OPERATOR_LOGICAL_OR;
    } else {
      return null;
    }
  }

  private void visitNestedChildren(UastNode node) {
    incrementNesting();
    visitChildren(node);
    decrementNesting();
  }

  private void visitChildren(UastNode node) {
    for (UastNode child : node.children) {
      visit(node, child);
    }
  }

  private static UastNode keyword(UastNode parent, UastNode node) {
    if (node.is(UastNode.Kind.ELSE)) {
      IfLike ifNode = IfLike.from(parent);
      if (ifNode != null) {
        UastNode elseKeyword = ifNode.elseKeyword();
        if (elseKeyword != null) {
          return elseKeyword;
        }
      }
    }
    return node.getChild(UastNode.Kind.KEYWORD).orElse(node);
  }

  public int value() {
    return complexity;
  }

  public Issue.Message[] secondaryLocations() {
    if (secondaryLocations != null) {
      return secondaryLocations.toArray(new Issue.Message[secondaryLocations.size()]);
    } else {
      return new Issue.Message[0];
    }
  }

  private void incrementNesting() {
    nesting++;
  }

  private void decrementNesting() {
    nesting--;
  }

  private void increaseComplexityByNesting(UastNode node) {
    increaseComplexity(node, nesting);
  }

  private void increaseComplexityByOne(UastNode node) {
    increaseComplexity(node, 1);
  }

  private void increaseComplexity(UastNode node, int increase) {
    this.complexity += increase;
    addSecondaryLocation(node, increase);
  }

  private void addSecondaryLocation(UastNode node, int increase) {
    if (secondaryLocations != null) {
      String message;
      if (increase == 1) {
        message = "+1";
      } else {
        message = "+" + increase + " (incl " + (increase - 1) + " for nesting)";
      }
      secondaryLocations.add(new Issue.Message(node, message));
    }
  }

}