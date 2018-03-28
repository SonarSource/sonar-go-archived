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
import java.util.List;
import java.util.Set;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.check.Rule;
import org.sonar.uast.UastNode;
import org.sonar.uast.helpers.CaseLike;
import org.sonar.uast.helpers.IfLike;
import org.sonar.uast.helpers.SwitchLike;

import static org.sonar.uast.Uast.syntacticallyEquivalent;

/**
 * https://jira.sonarsource.com/browse/RSPEC-3923
 */
@Rule(key = "S3923")
public class AllBranchesAreIdenticalCheck extends Check {

  private static final String MESSAGE = "Remove this conditional structure or edit its code blocks so that they're not all the same.";
  private Set<UastNode> visitedIfs = new HashSet<>();

  public AllBranchesAreIdenticalCheck() {
    super(UastNode.Kind.IF, UastNode.Kind.SWITCH);
  }

  @Override
  public void enterFile(InputFile inputFile) {
    visitedIfs.clear();
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
    if (caseNodes.size() < 2) {
      return;
    }
    UastNode firstCase = CaseLike.from(caseNodes.get(0)).body();
    if (caseNodes.stream().noneMatch(UastNode.Kind.DEFAULT_CASE)) {
      return;
    }
    boolean allEquivalent = caseNodes.stream()
      .skip(1)
      .map(caseNode -> CaseLike.from(caseNode).body())
      .allMatch(body -> syntacticallyEquivalent(firstCase, body));
    if (allEquivalent) {
      reportIssue(switchLike.switchKeyword(), MESSAGE);
    }
  }

  private void handleIf(UastNode node) {
    IfLike ifLike = IfLike.from(node);
    if (ifLike == null || visitedIfs.contains(node)) {
      return;
    }
    UastNode thenNode = ifLike.thenNode();
    UastNode elseNode = ifLike.elseNode();
    boolean allSame = true;
    IfLike elseIf = IfLike.from(elseNode);
    while (elseIf != null) {
      visitedIfs.add(elseIf.node());
      if (!syntacticallyEquivalent(thenNode, elseIf.thenNode())) {
        allSame = false;
      }
      elseNode = elseIf.elseNode();
      elseIf = IfLike.from(elseIf.elseNode());
    }
    if (elseNode == null) {
      // if without else is exception of the rule, see RSPEC
      return;
    }
    allSame = allSame && syntacticallyEquivalent(thenNode, elseNode);
    if (allSame) {
      reportIssue(ifLike.ifKeyword(), MESSAGE);
    }
  }
}
