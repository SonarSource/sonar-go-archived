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

import java.util.List;
import javax.annotation.Nullable;
import org.sonar.uast.UastNode;

public class CaseLike {

  private final UastNode node;
  private final List<UastNode> conditions;
  private final UastNode body;

  public CaseLike(UastNode caseNode, List<UastNode> conditions, @Nullable UastNode body) {
    this.node = caseNode;
    this.conditions = conditions;
    this.body = body;
  }

  public static CaseLike from(UastNode caseNode) {
    return new CaseLike(caseNode, caseNode.getChildren(UastNode.Kind.CONDITION), caseNode.getChild(UastNode.Kind.BLOCK).orElse(null));
  }

  public UastNode node() {
    return node;
  }

  public List<UastNode> conditions() {
    return conditions;
  }

  @Nullable
  public UastNode body() {
    return body;
  }
}
