package org.sonar.uast.helpers;

import java.io.StringReader;
import org.junit.jupiter.api.Test;
import org.sonar.uast.Uast;
import org.sonar.uast.UastNode;

import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

class LiteralLikeTest {

  @Test
  void test() {
    UastNode literal = Uast.from(new StringReader("{ \"kinds\": [\"LITERAL\"], \"token\": {\"value\": \"foo\" } }"));
    LiteralLike literalLike = LiteralLike.from(literal);
    assertThat(literalLike).isNotNull();
    assertThat(literalLike.value()).isEqualTo("foo");

    UastNode node = new UastNode(emptySet(), "", null, singletonList(literal));
    literalLike = LiteralLike.from(node);
    assertThat(literalLike).isNotNull();
    assertThat(literalLike.value()).isEqualTo("foo");
  }

  @Test
  void literal_nested_as_only_child() {
    UastNode literal = Uast.from(new StringReader(
      "{ \"kinds\": [], " +
        "\"children\": [{ \"kinds\": [\"LITERAL\"], \"token\": {\"value\": \"foo\" } }]" +
        "}"));

    LiteralLike literalLike = LiteralLike.from(literal);
    assertThat(literalLike).isNotNull();
    assertThat(literalLike.value()).isEqualTo("foo");
  }

  @Test
  void multiple_literal_children() {
    UastNode literal = Uast.from(new StringReader(
      "{ \"kinds\": [], " +
        "\"children\": [" +
          "{ \"kinds\": [\"LITERAL\"], \"token\": {\"value\": \"foo\" } }," +
          "{ \"kinds\": [\"LITERAL\"], \"token\": {\"value\": \"bar\" } }" +
        "]" +
        "}"));

    LiteralLike literalLike = LiteralLike.from(literal);
    assertThat(literalLike).isNull();
  }
}
