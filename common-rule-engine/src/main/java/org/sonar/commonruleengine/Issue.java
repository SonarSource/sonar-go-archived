package org.sonar.commonruleengine;

import java.util.Arrays;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.sonar.commonruleengine.checks.Check;
import org.sonar.uast.UastNode;

public class Issue {

  private final Check check;
  private final Message primaryMessage;
  private final Message[] secondaryMessages;

  public Issue(Check check, Message primaryMessage, Message... secondaryMessages) {
    this.check = check;
    this.primaryMessage = primaryMessage;
    this.secondaryMessages = secondaryMessages;
  }

  public static Issue issueOnFile(Check check, String message) {
    return new Issue(check, new Message(null, message));
  }

  public Check getCheck() {
    return check;
  }

  public boolean hasLocation() {
    return primaryMessage.from != null;
  }

  public String getMessage() {
    return primaryMessage.description;
  }

  public Message getPrimary() {
    return primaryMessage;
  }

  public Message[] getSecondaries() {
    return secondaryMessages;
  }

  @Override
  public String toString() {
    return check.getClass().getSimpleName() + ": " + primaryMessage.toString() + " " +
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

    /**
     *
     * @param node can be null for file issue
     * @param description
     */
    public Message(@Nullable UastNode node, @Nullable String description) {
      this(node, node, description);
    }

    public Message(@Nullable UastNode from, @Nullable UastNode to, @Nullable String description) {
      this.from = from;
      this.to = to;
      this.description = description;
    }

    @Override
    public String toString() {
      StringBuilder text = new StringBuilder();
      if (from != null) {
        text.append("(");
        text.append(from.firstToken());
        text.append(", ");
        text.append(to.lastToken());
        text.append(")");
      }
      if (description != null) {
        text.append(" ").append(description);
      }
      return text.toString();
    }
  }
}
