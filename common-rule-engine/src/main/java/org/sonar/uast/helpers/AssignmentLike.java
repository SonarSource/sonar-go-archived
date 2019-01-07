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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.annotation.CheckForNull;
import org.sonar.uast.UastNode;

public class AssignmentLike {

  public static final UastNode.Kind KIND = UastNode.Kind.ASSIGNMENT;

  private final UastNode node;
  private final UastNode target;
  private final UastNode operator;
  private final UastNode value;
  private final boolean multiple;
  private List<AssignmentLike> assignmentsTuples = null;

  private AssignmentLike(UastNode node, UastNode target, UastNode operator, UastNode value, boolean multiple) {
    this.node = node;
    this.target = target;
    this.operator = operator;
    this.value = value;
    this.multiple = multiple;
  }

  @CheckForNull
  public static AssignmentLike from(UastNode node) {
    if (node.is(AssignmentLike.KIND)) {
      Optional<UastNode> operator = node.getChild(UastNode.Kind.ASSIGNMENT_OPERATOR);
      if (!operator.isPresent()) {
        return null;
      }
      Optional<UastNode> target = node.getChild(UastNode.Kind.ASSIGNMENT_TARGET);
      Optional<UastNode> value = node.getChild(UastNode.Kind.ASSIGNMENT_VALUE);
      if (target.isPresent() && value.isPresent()) {
        return new AssignmentLike(node, target.get(), operator.get(), value.get(), false);
      }
      target = node.getChild(UastNode.Kind.ASSIGNMENT_TARGET_LIST);
      value = node.getChild(UastNode.Kind.ASSIGNMENT_VALUE_LIST);
      if (target.isPresent() && value.isPresent()) {
        return new AssignmentLike(node, target.get(), operator.get(), value.get(), true);
      }
    }
    return null;
  }

  public boolean isMultiple() {
    return multiple;
  }

  public List<AssignmentLike> assignmentsTuples() {
    if (assignmentsTuples != null) {
      return assignmentsTuples;
    }
    if (!multiple) {
      assignmentsTuples = Collections.singletonList(this);
    } else {
      List<UastNode> targets = target.getChildren(UastNode.Kind.ASSIGNMENT_TARGET);
      List<UastNode> values = value.getChildren(UastNode.Kind.ASSIGNMENT_VALUE);
      int nbTargets = targets.size();
      if (nbTargets != values.size()) {
        // malformed
        return Collections.emptyList();
      }
      List<AssignmentLike> result = new ArrayList<>(nbTargets);
      for (int i = 0; i < nbTargets; i++) {
        result.add(new AssignmentLike(node, targets.get(i), operator, values.get(i), false));
      }
      assignmentsTuples = Collections.unmodifiableList(result);
    }

    return assignmentsTuples;
  }

  public UastNode node() {
    return node;
  }

  public UastNode target() {
    return target;
  }

  public UastNode operator() {
    return operator;
  }

  public UastNode value() {
    return value;
  }
}
