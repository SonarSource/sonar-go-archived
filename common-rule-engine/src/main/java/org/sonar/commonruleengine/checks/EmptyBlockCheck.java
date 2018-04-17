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
import java.util.List;
import java.util.Set;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.check.Rule;
import org.sonar.uast.UastNode;
import org.sonar.uast.UastNode.Kind;
import org.sonar.uast.UastNode.Token;

/**
 * https://jira.sonarsource.com/browse/RSPEC-108
 */
@Rule(key = "S108")
public class EmptyBlockCheck extends Check {

  private static final String MESSAGE = "Either remove or fill this block of code.";

  private Set<UastNode> functionBlocks = new HashSet<>();
  private Set<UastNode> forAndSelectBlocks = new HashSet<>();

  public EmptyBlockCheck() {
    super(Kind.SWITCH, Kind.BLOCK, Kind.FUNCTION, Kind.FUNCTION_LITERAL, Kind.STATEMENT);
  }

  @Override
  public void enterFile(InputFile inputFile) throws IOException {
    functionBlocks.clear();
    forAndSelectBlocks.clear();
  }

  @Override
  public void visitNode(UastNode node) {
    if (node.is(Kind.STATEMENT)) {
      Token firstToken = node.firstToken();
      // "select" statement node in golang has a block as body
      // it should be excluded as "select {}" is commonly used for "sleep"
      // https://github.com/SonarSource/sonar-go/issues/258 will improve mapping of "select" statement
      if (firstToken != null && firstToken.value.equals("select")) {
        forAndSelectBlocks.addAll(node.getChildren(Kind.BLOCK));
      }
      // "for" loop is often used in golang with empty body for iterating until some condition is met
      // "foreach" with empty body is used in golang to empty the channel
      if (node.is(Kind.FOR, Kind.FOREACH)) {
        forAndSelectBlocks.addAll(node.getChildren(Kind.BLOCK));
      }
    }

    if (node.is(Kind.FUNCTION, Kind.FUNCTION_LITERAL)) {
      List<UastNode> children = node.getChildren(Kind.BLOCK);
      functionBlocks.addAll(children);
    }

    if (node.is(Kind.BLOCK, Kind.SWITCH) && !functionBlocks.contains(node) && !hasSomethingBetweenBraces(node)) {
      if (forAndSelectBlocks.contains(node)) {
        return;
      }
      reportIssue(node, MESSAGE);
    }
  }

  private static boolean hasSomethingBetweenBraces(UastNode node) {
    boolean insideBraces = false;
    boolean hasBraces = false;
    for (UastNode child : node.children) {
      if (child.token != null && child.token.value.equals("{")) {
        insideBraces = true;
        hasBraces = true;
      } else if (child.token != null && child.token.value.equals("}")) {
        insideBraces = false;
      } else if (insideBraces) {
        return true;
      }
    }

    return !hasBraces;
  }

}
