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

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.sonar.uast.UastNode;

import static org.assertj.core.api.Assertions.assertThat;

class IssueTest {

  @Test
  void message_to_string() {
    Set<UastNode.Kind> noKind = Collections.emptySet();
    List<UastNode> noChild = Collections.emptyList();
    UastNode node1 = new UastNode(noKind, "", new UastNode.Token(42, 7, "{"), noChild);
    UastNode node2 = new UastNode(noKind, "", new UastNode.Token(54, 3, "}"), noChild);
    Issue.Message message1 = new Issue.Message(node1, node2, "a message");
    assertThat(message1.toString()).isEqualTo("([42:7 {], [54:3 }]) a message");

    Issue.Message message2 = new Issue.Message(node1, node2, null);
    assertThat(message2.toString()).isEqualTo("([42:7 {], [54:3 }])");
  }
}
