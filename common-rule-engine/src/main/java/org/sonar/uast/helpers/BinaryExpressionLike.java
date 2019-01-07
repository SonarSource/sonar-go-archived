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
package org.sonar.uast.helpers;

import java.util.Optional;
import javax.annotation.CheckForNull;
import org.sonar.uast.UastNode;

public class BinaryExpressionLike {
  private final UastNode node;
  private final UastNode leftOperand;
  private final UastNode operator;
  private final UastNode rightOperand;

  public BinaryExpressionLike(UastNode node, UastNode leftOperand, UastNode operator, UastNode rightOperand) {
    this.node = node;
    this.leftOperand = leftOperand;
    this.operator = operator;
    this.rightOperand = rightOperand;
  }

  @CheckForNull
  public static BinaryExpressionLike from(UastNode node) {
    Optional<UastNode> leftOperand = node.getChild(UastNode.Kind.LEFT_OPERAND);
    Optional<UastNode> operator = node.getChild(UastNode.Kind.OPERATOR);
    Optional<UastNode> rightOperand = node.getChild(UastNode.Kind.RIGHT_OPERAND);
    if (leftOperand.isPresent() && operator.isPresent() && rightOperand.isPresent()) {
      return new BinaryExpressionLike(node, leftOperand.get(), operator.get(), rightOperand.get());
    }
    return null;
  }

  public UastNode node() {
    return node;
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
