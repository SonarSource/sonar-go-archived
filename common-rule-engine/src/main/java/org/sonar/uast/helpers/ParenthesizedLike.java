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


  private ParenthesizedLike(UastNode node, UastNode expression) {
    this.node = node;
    this.expression = expression;
  }

  @Nullable
  public static ParenthesizedLike from(UastNode node) {
    if (node.kinds.contains(UastNode.Kind.PARENTHESIZED_EXPRESSION)) {
      Optional<UastNode> expression = node.getChild(UastNode.Kind.EXPRESSION);
      if (expression.isPresent()) {
        return new ParenthesizedLike(node, expression.get());
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
}
