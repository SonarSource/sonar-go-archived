package org.sonar.uast.helpers;

import java.util.List;
import org.sonar.uast.UastNode;

public class CaseLike {

  private final UastNode node;
  private final List<UastNode> conditions;

  public CaseLike(UastNode caseNode, List<UastNode> conditions) {
    this.node = caseNode;
    this.conditions = conditions;
  }

  public static CaseLike from(UastNode caseNode) {
    return new CaseLike(caseNode, caseNode.getChildren(UastNode.Kind.CONDITION));
  }

  public UastNode node() {
    return node;
  }

  public List<UastNode> conditions() {
    return conditions;
  }
}
