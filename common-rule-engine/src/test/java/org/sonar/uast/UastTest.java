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
package org.sonar.uast;

import com.google.gson.JsonParseException;
import java.io.StringReader;
import java.util.Collections;
import java.util.EnumSet;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UastTest {

  @Test
  void parse_kind() throws Exception {
    UastNode node = Uast.from(new StringReader("{ kinds: [ 'IDENTIFIER', 'UNKNOWN1', 'IDENTIFIER', 'UNKNOWN2' ] }"));
    assertEquals(EnumSet.of(UastNode.Kind.IDENTIFIER), node.kinds);
  }

  @Test
  void parse_native_node() throws Exception  {
    UastNode node = Uast.from(new StringReader("{ nativeNode: 'foo' }"));
    assertEquals("foo", node.nativeNode);
  }

  @Test
  void parse_token() throws Exception  {
    UastNode node = Uast.from(new StringReader("{ token: { line: 1, column: 1, value: null } }"));
    assertEquals(Collections.emptyList(), node.children);
    assertNotNull(node.token);
    assertEquals(1, node.token.line);
    assertEquals(1, node.token.column);
    assertEquals("", node.token.value);

    node = Uast.from(new StringReader("{ token: { line: 12, column: 34, value: 'foo' } }"));
    assertNotNull(node.token);
    assertEquals(12, node.token.line);
    assertEquals(34, node.token.column);
    assertEquals("foo", node.token.value);

    node = Uast.from(new StringReader("{ kinds: [ 'EOF' ], token: { line: 345, column: 1 } }"));
    assertEquals(EnumSet.of(UastNode.Kind.EOF), node.kinds);
    assertNotNull(node.token);
    assertEquals(345, node.token.line);
    assertEquals(1, node.token.column);
    assertEquals("", node.token.value);
  }

  @Test
  void parse_invalid_token() {
    assertEquals("Attributes 'line' and 'column' are mandatory on 'token' object.",
      assertThrows(JsonParseException.class,
        () ->  Uast.from(new StringReader("{ token: { } }")))
        .getMessage());
  }

  @Test
  void parse_children() throws Exception  {
    UastNode node = Uast.from(new StringReader("{ children: [ { kinds: [ 'EOF' ] } ] }"));
    assertEquals(1, node.children.size());
    assertEquals(EnumSet.of(UastNode.Kind.EOF), node.children.get(0).kinds);
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "{}",
    "{ kinds: [], nativeNode: null, token: null, children: null, unknownElement: {} }",
    "{ kinds: [], nativeNode: '', token: null, children: [] }"
  })
  void parse_null_and_empty(String json) throws Exception  {
    Supplier<String> message = () -> "Assertion error for " + json;
    UastNode node = Uast.from(new StringReader(json));
    assertEquals(Collections.emptySet(), node.kinds, message);
    assertEquals("", node.nativeNode, message);
    assertNull(node.token, message);
    assertEquals(Collections.emptyList(), node.children, message);
  }

  @Test
  void syntactically_equivalent_of_unsupported_node() throws Exception  {
    UastNode node1 = Uast.from(new StringReader("{ children: [ { kinds: [ 'UNSUPPORTED' ] } ] }"));
    UastNode node2 = Uast.from(new StringReader("{ children: [ { kinds: [ 'UNSUPPORTED' ] } ] }"));
    assertFalse(Uast.syntacticallyEquivalent(node1, node2));
  }

}
