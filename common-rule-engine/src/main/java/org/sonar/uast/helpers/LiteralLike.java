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
import org.sonar.uast.UastNode;

public class LiteralLike {

  private final UastNode node;

  private LiteralLike(UastNode node) {
    this.node = node;
  }

  public static LiteralLike from(UastNode node) {
    if (node.kinds.contains(UastNode.Kind.LITERAL)) {
      return new LiteralLike(node);
    }
    if (node.children.size() == 1) {
      Optional<UastNode> childLiteral = node.getChild(UastNode.Kind.LITERAL);
      if (childLiteral.isPresent()) {
        return new LiteralLike(childLiteral.get());
      }
    }
    return null;
  }

  public String value() {
    return node.joinTokens();
  }

}
