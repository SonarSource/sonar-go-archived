/*
 * SonarQube Go Plugin
 * Copyright (C) 2018-2018 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
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
  @Nullable
  private final Double effortToFix;

  public Issue(Check check, Message primaryMessage, @Nullable Double effortToFix, Message... secondaryMessages) {
    this.check = check;
    this.primaryMessage = primaryMessage;
    this.effortToFix = effortToFix;
    this.secondaryMessages = secondaryMessages;
  }

  public static Issue issueOnFile(Check check, String message) {
    return new Issue(check, new Message(null, message), null);
  }

  public static Issue issueOnLine(Check check, int line, String message) {
    return new Issue(check, new Message(line, message), null);
  }

  public Check getCheck() {
    return check;
  }

  public boolean hasNodeLocation() {
    return primaryMessage.from != null;
  }

  public boolean hasLineLocation() {
    return primaryMessage.line != null;
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

  @Nullable
  public Double getEffortToFix() {
    return effortToFix;
  }

  @Override
  public String toString() {
    return check.getClass().getSimpleName() + ": " + primaryMessage.toString() + " " +
      Arrays.stream(secondaryMessages).map(Message::toString).collect(Collectors.joining(" "));
  }

  public static class Message {
    @Nullable
    public final UastNode from;
    @Nullable
    public final UastNode to;
    @Nullable
    public final Integer line;
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
      this.line = null;
    }

    public Message(int line, String message) {
      this.from = null;
      this.to = null;
      this.description = message;
      this.line = line;
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
