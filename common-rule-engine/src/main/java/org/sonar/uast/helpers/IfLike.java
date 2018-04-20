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
  private final UastNode ifKeyword;
  private final UastNode condition;
  private final UastNode thenNode;
  @Nullable
  private final ElseLike elseLike;

  private IfLike(UastNode node, UastNode ifKeyword, UastNode condition, UastNode thenNode, @Nullable ElseLike elseLike) {
    this.node = node;
    this.ifKeyword = ifKeyword;
    this.condition = condition;
    this.thenNode = thenNode;
    this.elseLike = elseLike;
  }

  @CheckForNull
  public static IfLike from(@Nullable UastNode node) {
    if (node == null) {
      return null;
    }
    if (node.is(UastNode.Kind.IF)) {
      Optional<UastNode> ifKeyword = node.getChild(UastNode.Kind.IF_KEYWORD);
      Optional<UastNode> condition = node.getChild(UastNode.Kind.CONDITION);
      Optional<UastNode> thenNode = node.getChild(UastNode.Kind.THEN);
      if (ifKeyword.isPresent() && condition.isPresent() && thenNode.isPresent()) {
        return new IfLike(node, ifKeyword.get(), condition.get(), thenNode.get(), ElseLike.from(node));
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
  public ElseLike elseLike() {
    return elseLike;
  }

  @CheckForNull
  public IfLike elseIf() {
    return elseLike != null ? elseLike.elseIf() : null;
  }

  public static class ElseLike {

    private final UastNode elseKeyword;
    private final UastNode elseNode;

    private ElseLike(UastNode elseKeyword, UastNode elseNode) {
      this.elseKeyword = elseKeyword;
      this.elseNode = elseNode;
    }

    @CheckForNull
    private static ElseLike from(@Nullable UastNode node) {
      if (node == null) {
        return null;
      }
      if (node.is(UastNode.Kind.IF)) {
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
}
