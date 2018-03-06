package org.sonar.uast;

import com.google.gson.JsonParseException;
import java.io.StringReader;
import java.util.Collections;
import java.util.EnumSet;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class UastTest {

  @Test
  void parse_kind() {
    UastNode node = Uast.from(new StringReader("{ kinds: [ 'IDENTIFIER', 'UNKNOWN1', 'IDENTIFIER', 'UNKNOWN2' ] }"));
    assertEquals(EnumSet.of(UastNode.Kind.IDENTIFIER), node.kinds);
  }

  @Test
  void parse_native_node() {
    UastNode node = Uast.from(new StringReader("{ nativeNode: 'foo' }"));
    assertEquals("foo", node.nativeNode);
  }

  @Test
  void parse_token() {
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
  void parse_children() {
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
  void parse_null_and_empty(String json) {
    Supplier<String> message = () -> "Assertion error for " + json;
    UastNode node = Uast.from(new StringReader(json));
    assertEquals(Collections.emptySet(), node.kinds, message);
    assertEquals("", node.nativeNode, message);
    assertNull(node.token, message);
    assertEquals(Collections.emptyList(), node.children, message);
  }

  @Test
  void syntactically_equivalent_of_unsupported_node() {
    UastNode node1 = Uast.from(new StringReader("{ children: [ { kinds: [ 'UNSUPPORTED' ] } ] }"));
    UastNode node2 = Uast.from(new StringReader("{ children: [ { kinds: [ 'UNSUPPORTED' ] } ] }"));
    assertFalse(Uast.syntacticallyEquivalent(node1, node2));
  }

}
