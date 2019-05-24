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
package org.sonar.commonruleengine.checks;

import java.util.List;
import java.util.ListIterator;
import org.sonar.check.Rule;
import org.sonar.uast.UastNode;

@Rule(key = "S1763")
public class UnreachableCodeCheck extends Check {

  public UnreachableCodeCheck() {
    super(UastNode.Kind.BLOCK);
  }

  @Override
  public void visitNode(UastNode node) {
    List<UastNode> statements = node.getChildren(UastNode.Kind.STATEMENT);
    ListIterator<UastNode> stmtIterator = statements.listIterator();
    while (stmtIterator.hasNext()) {
      UastNode statement = stmtIterator.next();
      if (statement.is(UastNode.Kind.UNCONDITIONAL_JUMP) && stmtIterator.hasNext()) {
        UastNode afterJump = stmtIterator.next();
        if (afterJump.isNot(UastNode.Kind.LABEL)) {
          reportIssue(statement, "Refactor this piece of code to not have any dead code after this " + statement.firstToken().value + ".");
        }
      }
    }
  }

}
