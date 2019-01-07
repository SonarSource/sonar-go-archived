/*
 * SonarQube Go Plugin
 * Copyright (C) 2018-2019 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
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
    if (binExpr.node().is(UastNode.Kind.EQUAL, UastNode.Kind.NOT_EQUAL, UastNode.Kind.LOGICAL_AND, UastNode.Kind.LOGICAL_OR)
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
