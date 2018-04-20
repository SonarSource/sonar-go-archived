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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.check.Rule;
import org.sonar.uast.UastNode;
import org.sonar.uast.UastNode.Kind;
import org.sonar.uast.helpers.ParenthesizedLike;

@Rule(key = "S2761")
public class DoublePrefixOperatorCheck extends Check {

  private static final Kind[] KINDS = new Kind[] {Kind.LOGICAL_COMPLEMENT, Kind.BITWISE_COMPLEMENT, Kind.UNARY_MINUS, Kind.UNARY_PLUS};
  private static final List<Kind> KIND_LIST = Arrays.asList(KINDS);
  private Set<UastNode> prefixSet = new HashSet<>();

  public DoublePrefixOperatorCheck() {
    super(KINDS);
  }

  @Override
  public void enterFile(InputFile inputFile) throws IOException {
    prefixSet.clear();
    super.enterFile(inputFile);
  }

  @Override
  public void visitNode(UastNode node) {
    if(prefixSet.contains(node)) {
      return;
    }
    node.kinds.stream().filter(KIND_LIST::contains)
      .findFirst().ifPresent(k -> checkDescendant(k, node));
  }

  private void checkDescendant(Kind kind, UastNode node) {
    Optional<UastNode> sameKindChildren = node.getChild(kind);
    if(sameKindChildren.isPresent()) {
      UastNode child = sameKindChildren.get();
      reportIssue(child);
      return;
    }
    node.getChild(Kind.PARENTHESIZED_EXPRESSION).ifPresent(child ->{
      UastNode expression = ParenthesizedLike.from(child).expression();
      if(expression.is(kind)) {
        reportIssue(expression);
      } else {
        checkDescendant(kind, expression);
      }
    });
  }

  private void reportIssue(UastNode child) {
    prefixSet.add(child);
    reportIssue(child, String.format("Use the \"%s\" operator just once or not at all.", child.firstToken().value));
  }
}
