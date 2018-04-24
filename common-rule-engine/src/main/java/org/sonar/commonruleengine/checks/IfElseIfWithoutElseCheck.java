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

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.check.Rule;
import org.sonar.uast.UastNode;
import org.sonar.uast.helpers.IfLike;

/**
 * https://jira.sonarsource.com/browse/RSPEC-126
 */
@Rule(key = "S126")
public class IfElseIfWithoutElseCheck extends Check {

  private Set<UastNode> visitedIf = new HashSet<>();

  public IfElseIfWithoutElseCheck() {
    super(UastNode.Kind.IF);
  }

  @Override
  public void enterFile(InputFile inputFile) throws IOException {
    visitedIf.clear();
    super.enterFile(inputFile);
  }

  @Override
  public void visitNode(UastNode node) {
    if(visitedIf.contains(node)) {
      return;
    }
    IfLike ifLike = IfLike.from(node);
    IfLike.ElseLike elseLike = ifLike.elseLike();
    if(elseLike == null) {
      // no else
      return;
    }
    IfLike elseIf = elseLike.elseIf();
    while (elseIf != null && elseLike != null) {
      visitedIf.add(elseIf.node());
      elseLike = elseIf.elseLike();
      if(elseLike != null) {
        elseIf = elseLike.elseIf();
      }
    }
    if(elseLike == null) {
      reportIssue(ifLike.ifKeyword(), "Add the missing else clause.");
    }
  }

}
