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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.commonruleengine.Issue;
import org.sonar.uast.UastNode;
import org.sonar.uast.helpers.LiteralLike;

/**
 * https://jira.sonarsource.com/browse/RSPEC-1192
 */
@Rule(key = "S1192")
public class StringLiteralDuplicatedCheck extends Check {

  private static final int DEFAULT_THRESHOLD = 3;

  // String literals include quotes, so this means length 5 as defined in RSPEC
  private static final int MINIMAL_LITERAL_LENGTH = 7;

  @RuleProperty(
    key = "threshold",
    description = "Number of times a literal must be duplicated to trigger an issue",
    defaultValue = "" + DEFAULT_THRESHOLD)
  public int threshold = DEFAULT_THRESHOLD;

  private final Map<String, List<UastNode>> occurrences = new HashMap<>();

  public StringLiteralDuplicatedCheck() {
    super(UastNode.Kind.STRING_LITERAL, UastNode.Kind.COMPILATION_UNIT);
  }

  @Override
  public void enterFile(InputFile inputFile) throws IOException {
    occurrences.clear();
    super.enterFile(inputFile);
  }

  @Override
  public void visitNode(UastNode node) {
    // Ignore Tags
    if (node.nativeNode.startsWith("Tag(")) {
      return;
    }

    LiteralLike literal = LiteralLike.from(node);
    if (literal != null) {
      String literalValue = literal.value();
      if (literalValue.length() > MINIMAL_LITERAL_LENGTH) {
        List<UastNode> occurrenceList = occurrences.computeIfAbsent(literalValue, key -> new ArrayList<>());
        occurrenceList.add(node);
      }
    }
  }

  @Override
  public void leaveNode(UastNode node) {
    if (node.is(UastNode.Kind.COMPILATION_UNIT)) {
      occurrences.values().stream()
        .filter(nodes -> nodes.size() >= threshold)
        .forEach(nodes -> {
          UastNode firstNode = nodes.iterator().next();
          reportIssue(firstNode, firstNode,
            "Define a constant instead of duplicating this literal " + nodes.size() + " times.", nodes.size(), secondaryMessages(nodes));
        });
    }
  }

  private Issue.Message[] secondaryMessages(List<UastNode> nodes) {
    return nodes.stream().skip(1).map(node -> new Issue.Message(node, "Duplication")).toArray(Issue.Message[]::new);
  }
}
