package org.sonar.uast;

import java.io.InputStreamReader;
import java.io.StringReader;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
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


  @Test
  void tokenize() {
    UastNode node = Uast.from(new StringReader("{ token: { line: 12, column: 34, value: 'foo' } }"));
    assertThat(node.joinTokens()).isEqualTo("foo");
  }

  @Test
  void tokenize2() {
    UastNode uastNode = Uast.from(new InputStreamReader(UastNodeTest.class.getResourceAsStream("/reference.java.uast.json")));
    assertThat(uastNode.joinTokens()).isEqualTo("class A {\n" +
      "  void foo() {\n" +
      "    System.out.println(\"yolo\");\n" +
      "  }\n" +
      "}\n");
  }
}
