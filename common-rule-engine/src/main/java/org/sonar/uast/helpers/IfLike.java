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

public class IfLike {

  private final UastNode node;
  private final UastNode condition;
  private final UastNode elseNode;
  private final UastNode thenNode;
  private final UastNode ifKeyword;

  public IfLike(UastNode node, UastNode ifKeyword, UastNode condition, UastNode thenNode, @Nullable UastNode elseNode) {
    this.node = node;
    this.ifKeyword = ifKeyword;
    this.condition = condition;
    this.elseNode = elseNode;
    this.thenNode = thenNode;
  }

  @CheckForNull
  public static IfLike from(@Nullable UastNode node) {
    if (node == null) {
      return null;
    }
    if (node.kinds.contains(UastNode.Kind.IF)) {
      Optional<UastNode> ifKeyword = node.getChild(UastNode.Kind.IF_KEYWORD);
      Optional<UastNode> condition = node.getChild(UastNode.Kind.CONDITION);
      Optional<UastNode> thenNode = node.getChild(UastNode.Kind.THEN);
      UastNode elseNode = node.getChild(UastNode.Kind.ELSE).orElse(null);
      if (ifKeyword.isPresent() && condition.isPresent() && thenNode.isPresent()) {
        return new IfLike(node, ifKeyword.get(), condition.get(), thenNode.get(), elseNode);
      }
    }
    return null;
  }

  public UastNode ifKeyword() {
    return ifKeyword;
  }

  public UastNode node() {
    return node;
  }

  public UastNode condition() {
    return condition;
  }

  public UastNode thenNode() {
    return thenNode;
  }

  @CheckForNull
  public UastNode elseNode() {
    return elseNode;
  }

  @CheckForNull
  public UastNode elseKeyword() {
    // TODO use kinds
    for (UastNode child : node.children) {
      UastNode.Token token = child.token;
      if (token != null && "else".equals(token.value)) {
        return child;
      }
    }
    return null;
  }

}
