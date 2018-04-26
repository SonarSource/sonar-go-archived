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

import org.sonar.check.Rule;
import org.sonar.uast.UastNode;
import org.sonar.uast.helpers.IfLike;

/**
 * https://jira.sonarsource.com/browse/RSPEC-1145
 */
@Rule(key = "S1145")
public class UselessIfCheck extends Check {

  public UselessIfCheck() {
    super(UastNode.Kind.IF);
  }

  @Override
  public void visitNode(UastNode node) {
    IfLike ifLike = IfLike.from(node);
    ifLike.condition().getChild(UastNode.Kind.BOOLEAN_LITERAL).ifPresent( n -> reportIssue(ifLike.ifKeyword(), "Remove this useless \"if\" statement."));
  }
}
