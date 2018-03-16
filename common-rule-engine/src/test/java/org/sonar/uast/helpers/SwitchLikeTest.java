package org.sonar.uast.helpers;

import java.io.StringReader;
import org.junit.jupiter.api.Test;
import org.sonar.uast.Uast;
import org.sonar.uast.UastNode;

import static org.assertj.core.api.Assertions.assertThat;

class SwitchLikeTest {

  @Test
  void test() throws Exception {
    UastNode node = Uast.from(new StringReader("{ kinds: ['SWITCH'] }"));
    SwitchLike switchLike = SwitchLike.from(node);
    assertThat(switchLike).isNotNull();
    assertThat(switchLike.caseNodes()).isEmpty();

    node = Uast.from(new StringReader("{ kinds: ['SWITCH'], children: [ { kinds: ['CASE'] }] }"));
    switchLike = SwitchLike.from(node);
    assertThat(switchLike).isNotNull();
    assertThat(switchLike.caseNodes()).hasSize(1);
  }
}
