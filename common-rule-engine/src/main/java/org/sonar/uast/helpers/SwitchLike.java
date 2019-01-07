/*
 * SonarQube Go Plugin
 * Copyright (C) 2018-2019 SonarSource SA
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

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.sonar.uast.UastNode;

public class SwitchLike {

  private final UastNode node;
  private final List<UastNode> caseNodes;

  public SwitchLike(UastNode node, List<UastNode> caseNodes) {
    this.node = node;
    this.caseNodes = caseNodes;
  }

  /**
   * Try to map a switch on any UastNode.
   *
   * Note that it's the responsibility of the caller to ensure that the returned object is null or not.
   * For subscription-based check on "switch" nodes, return value will never be null.
   *
   * @param node Any UastNode
   * @return null if node is not a switch, or an equivalent switch-like object
   */
  @Nullable
  public static SwitchLike from(UastNode node) {
    if (!node.is(UastNode.Kind.SWITCH)) {
      return null;
    }
    return new SwitchLike(node, getCases(node));
  }

  public UastNode node() {
    return node;
  }

  public UastNode switchKeyword() {
    return node.getChild(UastNode.Kind.KEYWORD).orElse(node);
  }

  public List<UastNode> caseNodes() {
    return caseNodes;
  }

  private static List<UastNode> getCases(UastNode switchNode) {
    List<UastNode> results = new ArrayList<>();
    // Collect all first level cases and avoid nested switches.
    // For some languages, cases are not direct children of the switch node in their AST
    switchNode.children.forEach(child -> child.getDescendants(UastNode.Kind.CASE, results::add, UastNode.Kind.SWITCH));
    return results;
  }
}
