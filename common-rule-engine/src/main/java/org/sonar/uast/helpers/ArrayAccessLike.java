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
import org.sonar.uast.UastNode;

public class ArrayAccessLike {

  public static final UastNode.Kind KIND = UastNode.Kind.ARRAY_ACCESS_EXPRESSION;

  private final UastNode node;
  private final UastNode objectExpression;
  private final UastNode keyExpression;

  private ArrayAccessLike(UastNode node, UastNode objectExpression, UastNode keyExpression) {
    this.node = node;
    this.objectExpression = objectExpression;
    this.keyExpression = keyExpression;
  }

  @CheckForNull
  public static ArrayAccessLike from(UastNode node) {
    if (node.kinds.contains(ArrayAccessLike.KIND)) {
      Optional<UastNode> objectExpression = node.getChild(UastNode.Kind.ARRAY_OBJECT_EXPRESSION);
      Optional<UastNode> keyExpression = node.getChild(UastNode.Kind.ARRAY_KEY_EXPRESSION);
      if (objectExpression.isPresent() && keyExpression.isPresent()) {
        return new ArrayAccessLike(node, objectExpression.get(), keyExpression.get());
      }
    }
    return null;
  }

  public UastNode node() {
    return node;
  }

  public UastNode objectExpression() {
    return objectExpression;
  }

  public UastNode keyExpression() {
    return keyExpression;
  }

}
