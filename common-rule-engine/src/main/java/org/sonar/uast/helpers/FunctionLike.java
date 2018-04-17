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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.annotation.CheckForNull;
import org.sonar.uast.UastNode;

public class FunctionLike {

  public static final UastNode.Kind KIND = UastNode.Kind.FUNCTION;
  private final UastNode node;
  private final UastNode name;
  private final UastNode block;

  private FunctionLike(UastNode node, UastNode name, UastNode block) {
    this.node = node;
    this.name = name;
    this.block = block;
  }

  @CheckForNull
  public static FunctionLike from(UastNode node) {
    if (node.kinds.contains(KIND)) {
      Optional<UastNode> nameNode = node.getChild(UastNode.Kind.FUNCTION_NAME);
      if (!nameNode.isPresent()) {
        return null;
      }
      return node.getChild(UastNode.Kind.BLOCK)
        .map(block -> new FunctionLike(node, nameNode.get(), block)).orElse(null);
    }
    return null;
  }

  public UastNode node() {
    return node;
  }

  public UastNode body() {
    return block;
  }

  public UastNode name() {
    return name;
  }

  public List<UastNode> resultList() {
    List<UastNode> result = new ArrayList<>();
    node.getDescendants(UastNode.Kind.RESULT_LIST, result::add, UastNode.Kind.BLOCK);
    return result;
  }

  public List<UastNode> parameters() {
    List<UastNode> parameters = node.getChildren(UastNode.Kind.PARAMETER);
    if (!parameters.isEmpty()) {
      return parameters;
    }
    return nestedParameters();
  }

  private List<UastNode> nestedParameters() {
    List<UastNode> parameters = new ArrayList<>();
    node.children.stream().forEach(child -> child.getDescendants(UastNode.Kind.PARAMETER, parameters::add, UastNode.Kind.BLOCK));
    return Collections.unmodifiableList(parameters);
  }
}
