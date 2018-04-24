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
package org.sonar.commonruleengine.checks;

import java.util.Optional;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.uast.UastNode;

@Rule(key = "S1186")
public class EmptyFunctionsCheck extends Check {

  public EmptyFunctionsCheck() {
    super(UastNode.Kind.FUNCTION, UastNode.Kind.FUNCTION_LITERAL);
  }

  @Override
  public void visitNode(UastNode node) {
    Optional<UastNode> block = node.getChild(UastNode.Kind.BLOCK);
    block.ifPresent(blck -> {
      if (blck.children.stream().allMatch(child -> isBraceBracket(child.token))) {
        reportIssue(node,
          "Add a nested comment explaining why this method is empty or complete the implementation.");
      }
    });
  }

  private static boolean isBraceBracket(@Nullable UastNode.Token token) {
    return token != null && ("{".equals(token.value) || "}".equals(token.value));
  }
}
