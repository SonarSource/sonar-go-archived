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

public class ForLike {

  public static final UastNode.Kind KIND = UastNode.Kind.FOR;

  private final UastNode node;
  private final UastNode forKeyword;
  @Nullable
  private final UastNode init;
  private final UastNode condition;
  @Nullable
  private final UastNode update;
  private final UastNode body;

  private ForLike(UastNode node, UastNode forKeyword, @Nullable UastNode init, UastNode condition,
                  @Nullable UastNode update, UastNode body) {
    this.node = node;
    this.forKeyword = forKeyword;
    this.init = init;
    this.condition = condition;
    this.update = update;
    this.body = body;
  }

  @CheckForNull
  public static ForLike from(UastNode node) {
    if (node.kinds.contains(ForLike.KIND)) {
      Optional<UastNode> forKeyword = node.getChild(UastNode.Kind.FOR_KEYWORD);
      Optional<UastNode> condition = node.getChild(UastNode.Kind.CONDITION);
      Optional<UastNode> body = node.getChild(UastNode.Kind.BODY);
      if (forKeyword.isPresent() && condition.isPresent() && body.isPresent()) {
        UastNode init = node.getChild(UastNode.Kind.FOR_INIT).orElse(null);
        UastNode update = node.getChild(UastNode.Kind.FOR_UPDATE).orElse(null);
        return new ForLike(node, forKeyword.get(), init, condition.get(), update, body.get());
      }
    }
    return null;
  }

  public UastNode node() {
    return node;
  }

  public UastNode forKeyword() {
    return forKeyword;
  }

  @CheckForNull
  public UastNode init() {
    return init;
  }

  public UastNode condition() {
    return condition;
  }

  @CheckForNull
  public UastNode update() {
    return update;
  }

  public UastNode body() {
    return body;
  }
}
