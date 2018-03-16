package org.sonar.uast.helpers;

import java.io.StringReader;
import org.junit.jupiter.api.Test;
import org.sonar.uast.Uast;
import org.sonar.uast.UastNode;

import static org.assertj.core.api.Assertions.assertThat;

class IfLikeTest {

  @Test
  void must_have_condition() throws Exception {
    UastNode node = Uast.from(new StringReader("{ kinds: ['IF'], children: [{kinds: ['CONDITION'], token: {value: 'cond', line: 1, column: 1}}] }"));
    IfLike ifLike = IfLike.from(node);
    assertThat(ifLike).isNotNull();
    assertThat(ifLike.condition().joinTokens()).isEqualTo("cond");

    node = Uast.from(new StringReader("{ kinds: ['IF'] }"));
    assertThat(IfLike.from(node)).isNull();
  }

  @Test
  void has_else() throws  Exception {
    UastNode node = Uast.from(new StringReader("{ kinds: ['IF'], " +
      "children: [" +
        "{kinds: ['CONDITION'], token: {value: 'cond', line: 1, column: 1}}," +
        "{kinds: ['ELSE'], token: {value: 'else', line: 1, column: 1}}" +
      "] }"));
    IfLike ifLike = IfLike.from(node);
    assertThat(ifLike).isNotNull();
    assertThat(ifLike.elseNode()).isNotNull();
    assertThat(ifLike.elseNode().joinTokens()).isEqualTo("else");
  }
}
