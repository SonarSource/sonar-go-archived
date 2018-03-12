package org.sonar.uast.helpers;

import java.io.StringReader;
import org.junit.jupiter.api.Test;
import org.sonar.uast.Uast;
import org.sonar.uast.UastNode;

import static org.assertj.core.api.Assertions.assertThat;

class CaseLikeTest {

  @Test
  void test() {
    UastNode node = Uast.from(new StringReader("{ kinds: ['CASE'], children: [{kinds: ['CONDITION']}, {kinds: ['CONDITION']}]}"));
    CaseLike caseLike = CaseLike.from(node);
    assertThat(caseLike).isNotNull();
    assertThat(caseLike.conditions()).hasSize(2);
  }
}
