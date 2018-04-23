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
package org.sonar.uast;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UastSpec {

  SpecBuilder b = new SpecBuilder();

  UastSpec() {
    SWITCH();
  }

  static UastSpec spec() {
    return new UastSpec();
  }

  List<Property> properties() {
    return b.build();
  }

  void SWITCH() {
    b.hasChild(UastNode.Kind.SWITCH, UastNode.Kind.KEYWORD);
    b.hasOptionallyChild(UastNode.Kind.SWITCH, UastNode.Kind.CASE);
  }
}

class SpecBuilder {

  List<Property> properties = new ArrayList<>();

  void hasChild(UastNode.Kind parent, UastNode.Kind child) {
    HasChild hasChild = new HasChild(parent, child, false);
    properties.add(hasChild);
  }

  void hasOptionallyChild(UastNode.Kind parent, UastNode.Kind child) {
    HasChild hasChild = new HasChild(parent, child, true);
    properties.add(hasChild);
  }

  List<Property> build() {
    return properties;
  }
}


class UastVerifier {

  static void verify(UastNode root, UastSpec spec) {
    List<Property> properties = spec.properties();
    properties.forEach(property -> verify(property, root));
    List<Property> untested = properties.stream()
      .filter(p -> !p.tested())
      .collect(Collectors.toList());
    if (!untested.isEmpty()) {
      String collect = untested.stream().map(Object::toString).collect(Collectors.joining("\n"));
      throw new IllegalStateException("Following properties were not tested:\n" + collect);
    }
  }

  private static void verify(Property property, UastNode root) {
    property.test(root);
    root.children.forEach(child -> verify(property, child));
  }
}


interface Property {

  void test(UastNode node);

  boolean tested();
}

class HasChild implements Property {

  private final UastNode.Kind parent;
  private final UastNode.Kind child;
  private final boolean optional;

  private boolean tested;

  HasChild(UastNode.Kind parent, UastNode.Kind child, boolean optional) {
    this.parent = parent;
    this.child = child;
    this.optional = optional;
  }

  @Override
  public void test(UastNode node) {
    if (node.is(parent) && !node.getChild(child).isPresent() && !optional) {
      throw new IllegalStateException("Property " + this + " not fulfilled " + node);
    }
    if (node.is(parent) && node.getChild(child).isPresent()) {
      tested = true;
    }
  }

  @Override
  public boolean tested() {
    // TODO when optional is true we should test for both positive and negative case
    return tested;
  }

  @Override
  public String toString() {
    String optionally = optional ? "optionally" : "";
    return "[" + parent + " has " + optionally + " child " + child + "]";
  }
}
