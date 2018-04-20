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
package org.sonar.uast.validators;

import java.util.List;
import java.util.Optional;
import org.sonar.uast.UastNode;
import org.sonar.uast.Visitor;

public abstract class Validator extends Visitor {

  private UastNode node;

  public Validator(UastNode.Kind targetKind) {
    super(targetKind);
  }

  @Override
  public final void visitNode(UastNode node) {
    this.node = node;
    validate(node);
  }

  public abstract void validate(UastNode node);

  private void fail(String errorMessage, Object... params) {
    throw new ValidationException(this.getClass().getSimpleName() + ": " + String.format(errorMessage, params));
  }

  public void is(UastNode.Kind... expectedKinds) {
    for (UastNode.Kind expected : expectedKinds) {
      if (node.isNot(expected)) {
        fail("Expected to have kind '%s' but got %s.", expected.name(), node.kinds);
      }
    }
  }

  public void hasKeyword(String keywordValue) {
    Optional<UastNode> keyword = node.getChild(UastNode.Kind.KEYWORD);
    if (!keyword.isPresent()) {
      fail("No keyword found.");
    } else {
      UastNode keywordNode = keyword.get();
      if (!keywordValue.equals(keywordNode.token.value)) {
        fail("Expected '%s' as keyword but got '%s'.", keywordValue, keywordNode.token.value);
      }
    }
  }

  public void hasAncestor(UastNode.Kind ancestorKind) {
    if (!node.getAncestor(ancestorKind).isPresent()) {
      fail("Should have a node of kind '%s' as ancestor.", ancestorKind);
    }
  }

  public void oneOrMoreChild(UastNode.Kind kind) {
    if (node.getChildren(kind).isEmpty()) {
      fail("Should have at least one child of kind '%s'.", kind);
    }
  }

  public void noChild(UastNode.Kind forbiddenKind) {
    if (!node.getChildren(forbiddenKind).isEmpty()) {
      fail("Should not have any child of kind '%s'.", forbiddenKind);
    }
  }

  public void singleChild(UastNode.Kind kind) {
    List<UastNode> children = node.getChildren(kind);
    if(children.isEmpty()) {
      fail("Should have one single child of kind '%s' but got none.", kind);
    }
    if (children.size() != 1) {
      fail("Should have one single child of kind '%s' but got %s.", kind, children);
    }
  }

  public static class ValidationException extends RuntimeException {

    public ValidationException(String message) {
      super(message);
    }
  }
}
