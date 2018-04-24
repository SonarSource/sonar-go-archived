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

import java.util.List;
import org.sonar.check.Rule;
import org.sonar.commonruleengine.Issue;
import org.sonar.uast.UastNode;
import org.sonar.uast.UastNode.Kind;

@Rule(key = "S1066")
public class CollapsibleIfCheck extends Check {

  private static final String MESSAGE = "Merge this if statement with the nested one.";

  public CollapsibleIfCheck() {
    super(Kind.IF);
  }

  @Override
  public void visitNode(UastNode node) {
    if (!hasElse(node)) {
      List<UastNode> bodyStatementChildren = node.getChildren(Kind.STATEMENT, Kind.BLOCK);
      if (bodyStatementChildren.size() == 1) {
        UastNode statementNode = bodyStatementChildren.get(0);
        if (statementNode.is(Kind.BLOCK)) {
          List<UastNode> nestedInBlockOptionalStatement = statementNode.getChildren(Kind.STATEMENT);
          if (nestedInBlockOptionalStatement.size() == 1) {
            statementNode = nestedInBlockOptionalStatement.get(0);
          }
        }

        if (statementNode.is(Kind.IF) && !hasElse(statementNode)) {
          reportIssue(
            getReportingLocationForIf(node),
            MESSAGE,
            new Issue.Message(getReportingLocationForIf(statementNode), "Nested \"if\" statement"));
        }
      }
    }
  }

  private static boolean hasElse(UastNode node) {
    return !node.getChildren(Kind.ELSE).isEmpty();
  }

  private static UastNode getReportingLocationForIf(UastNode ifStatementNode) {
    return ifStatementNode.getChild(Kind.IF_KEYWORD).orElse(ifStatementNode);
  }
}
