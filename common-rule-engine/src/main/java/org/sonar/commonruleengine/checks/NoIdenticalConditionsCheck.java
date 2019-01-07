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

import java.util.ArrayList;
import java.util.List;
import org.sonar.check.Rule;
import org.sonar.commonruleengine.Issue;
import org.sonar.uast.SyntacticEquivalence;
import org.sonar.uast.UastNode;
import org.sonar.uast.helpers.CaseLike;
import org.sonar.uast.helpers.IfLike;
import org.sonar.uast.helpers.SwitchLike;

import static org.sonar.uast.SyntacticEquivalence.areEquivalent;

/**
 * https://jira.sonarsource.com/browse/RSPEC-1862
 */
@Rule(key = "S1862")
public class NoIdenticalConditionsCheck extends Check {

  public NoIdenticalConditionsCheck() {
    super(UastNode.Kind.IF, UastNode.Kind.SWITCH);
  }

  @Override
  public void visitNode(UastNode node) {
    handleIf(node);
    handleSwitch(node);
  }

  private void handleSwitch(UastNode node) {
    SwitchLike switchLike = SwitchLike.from(node);
    if (switchLike == null) {
      return;
    }
    List<UastNode> caseNodes = switchLike.caseNodes();
    List<UastNode> allConditions = new ArrayList<>();
    for (UastNode caseNode : caseNodes) {
      CaseLike caseLike = CaseLike.from(caseNode);
      List<UastNode> conditions = caseLike.conditions();
      for (UastNode condition : conditions) {
        for (UastNode prevCondition : allConditions) {
          checkConditions(condition, prevCondition);
        }
        allConditions.add(condition);
      }
    }
  }

  private void handleIf(UastNode node) {
    IfLike ifLike = IfLike.from(node);
    if (ifLike == null) {
      return;
    }
    UastNode condition = ifLike.condition();
    IfLike elseIf = ifLike.elseIf();
    while (elseIf != null) {
      checkConditions(elseIf.condition(), condition);
      elseIf = elseIf.elseIf();
    }
  }

  private void checkConditions(UastNode condition, UastNode prevCondition) {
    UastNode.Token prevConditionToken = prevCondition.firstToken();
    if (prevConditionToken != null && SyntacticEquivalence.areEquivalent(condition, prevCondition)) {
      int prevConditionLine = prevConditionToken.line;
      reportIssue(condition, "This condition is same as one already tested on line " + prevConditionLine + ".",
        new Issue.Message(prevCondition, "Original"));
    }
  }
}
