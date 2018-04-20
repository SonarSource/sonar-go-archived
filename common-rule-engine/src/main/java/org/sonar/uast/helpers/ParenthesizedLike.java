/*
 * SonarQube Go Plugin
 * Copyright (C) 2018-2018 SonarSource SA
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
import javax.annotation.Nullable;
import org.sonar.uast.UastNode;

public class ParenthesizedLike {

  private final UastNode node;
  private final UastNode expression;
  private final UastNode leftParenthesis;
  private final UastNode rightParenthesis;

  private ParenthesizedLike(UastNode node, UastNode leftParenthesis, UastNode expression, UastNode rightParenthesis) {
    this.node = node;
    this.leftParenthesis = leftParenthesis;
    this.expression = expression;
    this.rightParenthesis = rightParenthesis;
  }

  @Nullable
  public static ParenthesizedLike from(UastNode node) {
    if (node.is(UastNode.Kind.PARENTHESIZED_EXPRESSION)) {
      Optional<UastNode> leftParenthesis = node.getChild(UastNode.Kind.LEFT_PARENTHESIS);
      Optional<UastNode> expression = node.getChild(UastNode.Kind.EXPRESSION);
      Optional<UastNode> rightParenthesis = node.getChild(UastNode.Kind.RIGHT_PARENTHESIS);
      if (leftParenthesis.isPresent() && expression.isPresent() && rightParenthesis.isPresent()) {
        return new ParenthesizedLike(node, leftParenthesis.get(), expression.get(), rightParenthesis.get());
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

  public UastNode leftParenthesis() {
    return leftParenthesis;
  }

  public UastNode rightParenthesis() {
    return rightParenthesis;
  }

}
