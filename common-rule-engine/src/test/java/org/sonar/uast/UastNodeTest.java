package org.sonar.uast;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UastNodeTest {

  @Test
  void invalid_token_position() {
    assertEquals("Invalid token location 0:0",
      assertThrows(IllegalArgumentException.class,
        () -> new UastNode.Token(0, 0, "token"))
          .getMessage());
  }

  @Test
  void token_position() {
    UastNode.Token token = new UastNode.Token(2, 4, "if");
    assertEquals(2, token.line);
    assertEquals(4, token.column);
    assertEquals(2, token.endLine);
    assertEquals(5, token.endColumn);

    token = new UastNode.Token(10, 1, "/* start \n end */");
    assertEquals(10, token.line);
    assertEquals(1, token.column);
    assertEquals(11, token.endLine);
    assertEquals(7, token.endColumn);
  }
}
