package org.sonar.commonruleengine;

import java.util.Arrays;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.sonar.commonruleengine.checks.Check;
import org.sonar.uast.UastNode;

public class Issue {

  private final Check rule;
  private final Message primaryMessage;
  private final Message[] secondaryMessages;

  public Issue(Check rule, Message primaryMessage, Message... secondaryMessages) {
    this.rule = rule;
    this.primaryMessage = primaryMessage;
    this.secondaryMessages = secondaryMessages;
  }

  public Check getRule() {
    return rule;
  }

  public Message getPrimary() {
    return primaryMessage;
  }

  public Message[] getSecondaries() {
    return secondaryMessages;
  }

  @Override
  public String toString() {
    return rule.getClass().getSimpleName() + ": " + primaryMessage.toString() + " " +
      Arrays.stream(secondaryMessages).map(Message::toString).collect(Collectors.joining(" "));
  }

  public static class Message {
    public final UastNode from;
    public final UastNode to;
    @Nullable
    public final String description;

    public Message(UastNode node) {
      this(node, node, null);
    }

    public Message(UastNode node, @Nullable String description) {
      this(node, node, description);
    }

    public Message(UastNode from, UastNode to, @Nullable String description) {
      this.from = from;
      this.to = to;
      this.description = description;
    }

    @Override
    public String toString() {
      return "(" + this.from.firstToken() + ", " + this.to.lastToken() + ") " + description;
    }
  }
}
