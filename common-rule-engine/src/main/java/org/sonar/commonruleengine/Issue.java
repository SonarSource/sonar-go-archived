package org.sonar.commonruleengine;

import org.sonar.commonruleengine.rules.CommonRule;
import org.sonar.uast.UastNode;

public class Issue {

  private final String message;
  private final CommonRule rule;
  private final UastNode node;

  public Issue(CommonRule rule, UastNode node, String message) {
    this.message = message;
    this.node = node;
    this.rule = rule;
  }

  public String getMessage() {
    return message;
  }

  public CommonRule getRule() {
    return rule;
  }

  public UastNode getNode() {
    return node;
  }

  @Override
  public String toString() {
    return String.format("%s %s: %s", node, rule.getClass().getSimpleName(), message);
  }
}
