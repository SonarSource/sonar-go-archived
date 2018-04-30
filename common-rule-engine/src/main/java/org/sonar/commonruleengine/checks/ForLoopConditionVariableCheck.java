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

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.uast.UastNode;
import org.sonar.uast.UastNode.Kind;

@Rule(key = "S1994")
public class ForLoopConditionVariableCheck extends Check {

  private static final String MESSAGE = "This loop's stop condition tests variable which is not incremented in update clause.";

  public ForLoopConditionVariableCheck() {
    super(Kind.FOR);
  }

  @Override
  public void visitNode(UastNode node) {
    Optional<UastNode> optionalUpdateClause = node.getChild(Kind.FOR_UPDATE);
    Optional<UastNode> optionalConditionClause = node.getChild(Kind.CONDITION);
    if (optionalConditionClause.isPresent() && optionalUpdateClause.isPresent()) {
      Set<String> identifiersInCondition = getIdentifierNames(optionalConditionClause.get());
      Set<String> identifiersInUpdate = getIdentifierNames(optionalUpdateClause.get());
      Set<String> intersection = new HashSet<>(identifiersInCondition);
      if (identifiersInCondition.isEmpty() || identifiersInCondition.size() > 2) {
        return;
      }
      intersection.retainAll(identifiersInUpdate);
      if (intersection.isEmpty()) {
        reportIssue(node.getChild(Kind.KEYWORD).orElse(node), MESSAGE);
      }
    }
  }

  private static Set<String> getIdentifierNames(UastNode node) {
    Set<String> identifierNames = new HashSet<>();
    node.getDescendants(Kind.IDENTIFIER, identifier -> identifierNames.add(identifier.token.value));
    return identifierNames;
  }
}
