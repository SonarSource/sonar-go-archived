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
package org.sonar.uast.validators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.sonar.uast.UastNode;
import org.sonar.uast.UastNode.Token;

public final class ValidatorTestUtils {

  private ValidatorTestUtils() {
    // assertions
  }

  public static UastNode node(UastNode.Kind kind, UastNode... children) {
    return node(Collections.singleton(kind), children);
  }

  public static UastNode node(Set<UastNode.Kind> kinds, UastNode... children) {
    List<UastNode> childrenAsList = Collections.emptyList();
    if (children != null) {
      childrenAsList = Arrays.asList(children);
    }
    return new UastNode(kinds, "", null, childrenAsList);
  }

  public static UastNode keyword(String value, UastNode.Kind... additionalKinds) {
    Set<UastNode.Kind> kinds = new HashSet<>(Arrays.stream(additionalKinds).collect(Collectors.toSet()));
    kinds.add(UastNode.Kind.KEYWORD);
    return new UastNode(kinds, "", mockToken(value), Collections.emptyList());
  }

  public static UastNode token(String value) {
    return new UastNode(Collections.emptySet(), "", mockToken(value), Collections.emptyList());
  }

  private static Token mockToken(String value) {
    return new UastNode.Token(1, 1, value);
  }


  public static class NodeBuilder {

    Set<UastNode.Kind> kinds = EnumSet.noneOf(UastNode.Kind.class);
    private String nativeNode;
    private String tokenValue;
    private List<UastNode> children = new ArrayList<>();

    private NodeBuilder(UastNode.Kind... kinds) {
      this.kinds.addAll(Arrays.asList(kinds));
    }

    static NodeBuilder buildNode(UastNode.Kind... kinds) {
      return new NodeBuilder(kinds);
    }

    NodeBuilder addKind(UastNode.Kind kind) {
      kinds.add(kind);
      return this;
    }

    NodeBuilder addChildren(UastNode... child) {
      children.addAll(Arrays.asList(child));
      return this;
    }

    NodeBuilder nativeNode(String nativeNode) {
      this.nativeNode = nativeNode;
      return this;
    }

    NodeBuilder tokenValue(String tokenValue) {
      this.tokenValue = tokenValue;
      return this;
    }

    UastNode build() {
      return new UastNode(kinds, nativeNode, tokenValue == null ? null : mockToken(tokenValue), children);
    }
  }
}
