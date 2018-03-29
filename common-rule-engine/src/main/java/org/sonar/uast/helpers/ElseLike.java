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
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.uast.UastNode;

public class ElseLike {

  private final UastNode elseKeyword;
  private final UastNode elseNode;

  public ElseLike(UastNode elseKeyword, UastNode elseNode) {
    this.elseKeyword = elseKeyword;
    this.elseNode = elseNode;
  }

  @CheckForNull
  public static ElseLike from(@Nullable UastNode node) {
    if (node == null) {
      return null;
    }
    if (node.kinds.contains(UastNode.Kind.IF)) {
      Optional<UastNode> elseKeyword = node.getChild(UastNode.Kind.ELSE_KEYWORD);
      Optional<UastNode> elseNode = node.getChild(UastNode.Kind.ELSE);
      if (elseKeyword.isPresent() && elseNode.isPresent()) {
        return new ElseLike(elseKeyword.get(), elseNode.get());
      }
    }
    return null;
  }

  public UastNode elseKeyword() {
    return elseKeyword;
  }

  public UastNode elseNode() {
    return elseNode;
  }

  @CheckForNull
  public IfLike elseIf() {
    return IfLike.from(elseNode);
  }

}
