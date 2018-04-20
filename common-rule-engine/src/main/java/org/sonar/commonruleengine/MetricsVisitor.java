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
package org.sonar.commonruleengine;

import java.util.Set;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import org.sonar.uast.UastNode;

public class MetricsVisitor {

  private Metrics metrics;

  public void enterFile(UastNode uast) {
    metrics = new Metrics();
    metrics.cognitiveComplexity = CognitiveComplexity.calculateFileComplexity(uast).value();
  }

  public void visitNode(UastNode node) {
    if (node.is(UastNode.Kind.CLASS)) {
      metrics.numberOfClasses++;
    }
    if (node.is(UastNode.Kind.FUNCTION)) {
      metrics.numberOfFunctions++;
    }
    if (node.kinds.contains(UastNode.Kind.STATEMENT)) {
      metrics.numberOfStatements++;
    }
    if (node.is(UastNode.Kind.STATEMENT, UastNode.Kind.EXPRESSION, UastNode.Kind.CASE, UastNode.Kind.LABEL)) {
      addLines(metrics.executableLines, node.firstToken());
    }
    UastNode.Token token = node.token;
    if (token != null) {
      visitToken(node, token);
    }
  }

  private void visitToken(UastNode node, UastNode.Token token) {
    if (node.is(UastNode.Kind.EOF)) {
      return;
    }
    Set<Integer> lineNumbers = node.is(UastNode.Kind.COMMENT) ? metrics.commentLines : metrics.linesOfCode;
    addLines(lineNumbers, token);
  }

  private static void addLines(Set<Integer> lineNumbers, @Nullable UastNode.Token token) {
    if (token != null) {
      IntStream.range(token.line, token.endLine + 1).forEach(lineNumbers::add);
    }
  }

  public Metrics getMetrics() {
    return metrics;
  }

}
