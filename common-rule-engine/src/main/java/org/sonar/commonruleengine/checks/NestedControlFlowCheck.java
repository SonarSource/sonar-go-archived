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

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.commonruleengine.Issue;
import org.sonar.uast.UastNode;

@Rule(key = "S134")
public class NestedControlFlowCheck extends Check {

  private static final int DEFAULT_MAX = 4;

  @RuleProperty(
    description = "Maximum allowed control flow statement nesting depth.",
    defaultValue = "" + DEFAULT_MAX)
  public int max = DEFAULT_MAX;

  private Deque<UastNode> nesting = new ArrayDeque<>();

  public NestedControlFlowCheck() {
    super(UastNode.Kind.CONDITIONAL_JUMP);
  }

  @Override
  public void enterFile(InputFile inputFile) throws IOException {
    // in theory this is not necessary, just a safeguard
    nesting.clear();
  }

  @Override
  public void visitNode(UastNode node) {
    checkNesting(node);
    nesting.push(node);
  }

  @Override
  public void leaveNode(UastNode node) {
    nesting.pop();
  }

  private void checkNesting(UastNode node) {
    if (nesting.size() == max) {
      Issue.Message[] secondary = nesting.stream()
        .map(n -> new Issue.Message(n.getChild(UastNode.Kind.KEYWORD).orElse(node), "Nesting +1"))
        .toArray(Issue.Message[]::new);
      reportIssue(node.getChild(UastNode.Kind.KEYWORD).orElse(node),
        "Refactor this code to not nest more than " + max + " control flow statements.",
        secondary);
    }
  }
}
