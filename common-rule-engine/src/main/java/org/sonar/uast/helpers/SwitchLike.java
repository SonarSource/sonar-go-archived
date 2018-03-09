package org.sonar.uast.helpers;

import java.util.List;
import javax.annotation.CheckForNull;
import org.sonar.uast.UastNode;

public class SwitchLike {

  private final UastNode node;
  private final List<UastNode> caseNodes;

  public SwitchLike(UastNode node, List<UastNode> caseNodes) {
    this.node = node;
    this.caseNodes = caseNodes;
  }

  @CheckForNull
  public static SwitchLike from(UastNode node) {
    if (!node.kinds.contains(UastNode.Kind.SWITCH)) {
      return null;
    }
    return new SwitchLike(node, node.getChildren(UastNode.Kind.CASE));
  }

  public UastNode node() {
    return node;
  }

  public List<UastNode> caseNodes() {
    return caseNodes;
  }
}
