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
