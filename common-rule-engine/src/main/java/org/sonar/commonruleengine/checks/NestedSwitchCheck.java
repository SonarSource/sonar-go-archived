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

import java.util.HashSet;
import java.util.Set;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.check.Rule;
import org.sonar.uast.UastNode;
import org.sonar.uast.helpers.SwitchLike;

/**
 * https://jira.sonarsource.com/browse/RSPEC-1821
 */
@Rule(key = "S1821")
public class NestedSwitchCheck extends Check {

  private final Set<UastNode> reported = new HashSet<>();

  public NestedSwitchCheck() {
    super(UastNode.Kind.SWITCH);
  }

  @Override
  public void enterFile(InputFile inputFile) {
    reported.clear();
  }

  @Override
  public void visitNode(UastNode node) {
    reported.add(node);
    node.getDescendants(UastNode.Kind.SWITCH, this::checkNested, UastNode.Kind.FUNCTION_LITERAL, UastNode.Kind.CLASS);
  }

  public void checkNested(UastNode nestedSwitchNode) {
    if (!reported.contains(nestedSwitchNode)) {
      UastNode switchKeyword = SwitchLike.from(nestedSwitchNode).switchKeyword();
      reportIssue(switchKeyword, "Refactor the code to eliminate this nested \"switch\".");
      reported.add(nestedSwitchNode);
    }
  }

}
