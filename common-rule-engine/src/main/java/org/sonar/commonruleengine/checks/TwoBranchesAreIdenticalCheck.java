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
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.check.Rule;
import org.sonar.commonruleengine.Issue;
import org.sonar.uast.SyntacticEquivalence;
import org.sonar.uast.UastNode;
import org.sonar.uast.UastNode.Kind;
import org.sonar.uast.UastNode.Token;
import org.sonar.uast.helpers.CaseLike;
import org.sonar.uast.helpers.IfLike;
import org.sonar.uast.helpers.SwitchLike;

@Rule(key = "S1871")
public class TwoBranchesAreIdenticalCheck extends Check {

  private static final String MESSAGE = "This %s's code block is the same as the block for the %s on line %s.";
  private Set<UastNode> visitedIfs = new HashSet<>();

  public TwoBranchesAreIdenticalCheck() {
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
    List<UastNode> caseBodies = caseNodes.stream()
      .map(CaseLike::from)
      .filter(Objects::nonNull)
      .map(CaseLike::body)
      .collect(Collectors.toList());

    for (int i = 1; i < caseBodies.size(); i++) {
      for (int j = 0; j < i; j++) {
        UastNode duplicating = caseBodies.get(i);
        UastNode duplicated = caseBodies.get(j);
        if (duplicated != null && SyntacticEquivalence.areEquivalent(duplicating, duplicated) && isBigEnough(duplicated)) {
          reportIssue(caseNodes.get(i), caseNodes.get(j), "case");
          break;
        }
      }
    }
  }

  private void handleIf(UastNode node) {
    IfLike topIfStatement = IfLike.from(node);
    if (topIfStatement == null || visitedIfs.contains(node)) {
      return;
    }

    // "getIfBranches" also marks chained if-s as visited
    List<UastNode> ifBlocks = getIfBranches(topIfStatement);

    for (int i = 1; i < ifBlocks.size(); i++) {
      for (int j = 0; j < i; j++) {
        UastNode duplicating = ifBlocks.get(i);
        UastNode duplicated = ifBlocks.get(j);
        if (SyntacticEquivalence.areEquivalent(duplicating, duplicated) && isBigEnoughBlock(duplicated)) {
          reportIssue(duplicating, duplicated, "branch");
          break;
        }
      }
    }
  }

  private List<UastNode> getIfBranches(IfLike topIfStatement) {
    IfLike.ElseLike elseLike = topIfStatement.elseLike();
    IfLike elseIf = topIfStatement.elseIf();
    List<UastNode> ifBlocks = new ArrayList<>();
    ifBlocks.add(topIfStatement.thenNode());
    while (elseIf != null) {
      visitedIfs.add(elseIf.node());
      ifBlocks.add(elseIf.thenNode());
      elseLike = elseIf.elseLike();
      elseIf = elseIf.elseIf();
    }
    if (elseLike != null) {
      ifBlocks.add(elseLike.elseNode());
    }
    return ifBlocks;
  }

  private static boolean isBigEnough(UastNode node) {
    Token firstToken = node.firstToken();
    Token lastToken = node.lastToken();
    return firstToken != null && lastToken != null && lastToken.line - firstToken.line > 0;
  }

  private static boolean isBigEnoughBlock(UastNode node) {
    List<UastNode> statements = node.getChildren(Kind.STATEMENT);
    if (statements.isEmpty()) {
      return false;
    }
    Token firstToken = statements.get(0).firstToken();
    Token lastToken = statements.get(statements.size() - 1).lastToken();
    return firstToken != null && lastToken != null && lastToken.line - firstToken.line > 0;
  }

  private void reportIssue(UastNode duplicating, UastNode duplicated, String caseOrBranch) {
    Token firstToken = duplicated.firstToken();
    if (firstToken != null) {
      int duplicatedBranchFirstLine = firstToken.line;
      reportIssue(
        duplicating,
        String.format(MESSAGE, caseOrBranch, caseOrBranch, duplicatedBranchFirstLine),
        new Issue.Message(duplicated, "Original"));
    }
  }

}
