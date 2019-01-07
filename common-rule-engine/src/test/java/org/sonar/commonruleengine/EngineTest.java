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
package org.sonar.commonruleengine;

import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.commonruleengine.checks.Check;
import org.sonar.uast.UastNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EngineTest {

  private UastNode uast;

  @BeforeEach
  void setUp() throws Exception {
    uast = UastNode.from(new InputStreamReader(getClass().getResourceAsStream("/uast.json")));
  }

  @Test
  void visit_should_visit_all_nodes() throws Exception {
    NodeCounter nodeCounter = new NodeCounter();
    Engine engine = new Engine(Collections.singletonList(nodeCounter));
    InputFile inputFile = TestInputFileBuilder.create(".", "foo.go").setType(InputFile.Type.MAIN).build();
    List<Issue> issues = engine.scan(uast, inputFile).issues;
    assertEquals(4, issues.size());
    assertTrue(issues.stream().map(Issue::getCheck).allMatch(rule -> rule == nodeCounter));
  }

  static class NodeCounter extends Check {
    int count;

    NodeCounter() {
      super(UastNode.Kind.values());
    }

    @Override
    public void visitNode(UastNode node) {
      count++;
      reportIssue(node, String.valueOf(count));
    }
  }
}
