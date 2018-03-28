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

public class AssignmentLike {

  public static final UastNode.Kind KIND = UastNode.Kind.ASSIGNMENT;

  private final UastNode node;
  private final UastNode target;
  private final UastNode value;

  private AssignmentLike(UastNode node, UastNode target, UastNode value) {
    this.node = node;
    this.target = target;
    this.value = value;
  }

  @CheckForNull
  public static AssignmentLike from(UastNode node) {
    if (node.kinds.contains(AssignmentLike.KIND)) {
      Optional<UastNode> target = node.getChild(UastNode.Kind.ASSIGNMENT_TARGET);
      Optional<UastNode> value = node.getChild(UastNode.Kind.ASSIGNMENT_VALUE);
      if (target.isPresent() && value.isPresent()) {
        return new AssignmentLike(node, target.get(), value.get());
      }
    }
    return null;
  }

  public UastNode node() {
    return node;
  }

  public UastNode target() {
    return target;
  }

  public UastNode value() {
    return value;
  }
}
