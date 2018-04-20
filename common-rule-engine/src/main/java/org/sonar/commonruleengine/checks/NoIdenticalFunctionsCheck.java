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

import java.util.ArrayList;
import java.util.List;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.check.Rule;
import org.sonar.commonruleengine.Issue;
import org.sonar.uast.SyntacticEquivalence;
import org.sonar.uast.UastNode;
import org.sonar.uast.helpers.FunctionLike;

import static org.sonar.uast.SyntacticEquivalence.areEquivalent;

/**
 * Rule https://jira.sonarsource.com/browse/RSPEC-4144
 */
@Rule(key = "S4144")
public class NoIdenticalFunctionsCheck extends Check {

  private List<FunctionLike> functions = new ArrayList<>();

  public NoIdenticalFunctionsCheck() {
    super(UastNode.Kind.FUNCTION, UastNode.Kind.CLASS);
  }

  @Override
  public void enterFile(InputFile inputFile) {
    functions.clear();
  }

  @Override
  public void visitNode(UastNode node) {
    if (node.kinds.contains(UastNode.Kind.CLASS)) {
      functions.clear();
    }
    if (node.kinds.contains(FunctionLike.KIND)) {
      FunctionLike thisFunction = FunctionLike.from(node);
      if (thisFunction == null) {
        return;
      }
      if (thisFunction.body().getChildren(UastNode.Kind.STATEMENT).size() < 2) {
        return;
      }
      for (FunctionLike function : functions) {
        if (SyntacticEquivalence.areEquivalent(thisFunction.body(), function.body())
          && areEquivalent(thisFunction.parameters(), function.parameters())
          && areEquivalent(thisFunction.resultList(), function.resultList())) {
          reportIssue(thisFunction.name(),
            "Function is identical with function on line " + function.node().firstToken().line + ".",
            new Issue.Message(function.name(), "Original implementation"));
          break;
        }
      }
      functions.add(thisFunction);
    }
  }
}
