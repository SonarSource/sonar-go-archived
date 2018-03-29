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
package org.sonar.uast;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public final class UastNode {

  public final Set<Kind> kinds;
  public final String nativeNode;
  @Nullable
  public final Token token;
  public final List<UastNode> children;

  public UastNode(Set<Kind> kinds, String nativeNode, @Nullable Token token, List<UastNode> children) {
    this.kinds = kinds;
    this.nativeNode = nativeNode;
    this.token = token;
    this.children = children;
  }

  public static class Token {

    private static final Pattern LINE_SPLITTER = Pattern.compile("\r\n|\n|\r");

    public final String value;
    /**
     * start at 1, line number of the first character of the token
     */
    public final int line;
    /**
     * start at 1, column number of the first character of the token
     */
    public final int column;
    /**
     * start at 1, line number of the last character of the token
     * if the token is on a single line, endLine() == line()
     */
    public final int endLine;
    /**
     * start at 1, column number of the last character of the token
     * if the token has only one character, endColumn() == column()
     * EOF token is empty, in this case endColumn() == column() - 1
     */
    public final int endColumn;

    public Token(int line, int column, String value) {
      if (line < 1 || column < 1) {
        throw new IllegalArgumentException("Invalid token location " + line + ":" + column);
      }
      this.line = line;
      this.column = column;
      this.value = value;
      if (value.indexOf('\n') == -1 && value.indexOf('\r') == -1) {
        this.endLine = line;
        this.endColumn = column + codePointCount(value) - 1;
      } else {
        String[] lines = LINE_SPLITTER.split(value, -1);
        this.endLine = line + lines.length - 1;
        this.endColumn = codePointCount(lines[lines.length - 1]);
      }
    }

    private static int codePointCount(String s) {
      // handle length of UTF-32 encoded strings properly
      // s.length() would return 2 for each UTF-32 character, which will mess with column computation
      return s.codePointCount(0, s.length());
    }

    @Override
    public String toString() {
      return "[" + line + ":" + column + " " + value + "]";
    }
  }

  public enum Kind implements Predicate<UastNode> {
    ASSIGNMENT,
    ASSIGNMENT_OPERATOR,
    ASSIGNMENT_TARGET,
    ASSIGNMENT_VALUE,
    EXPRESSION,
    LABEL,
    BINARY_EXPRESSION,
    PARENTHESIZED_EXPRESSION,
    LEFT_PARENTHESIS,
    RIGHT_PARENTHESIS,
    BLOCK,
    BREAK,
    CASE,
    CLASS,
    COMMENT,
    COMPOUND_ASSIGNMENT,
    CONTINUE,
    DEFAULT_CASE,
    STRUCTURED_COMMENT,
    COMPILATION_UNIT,
    CONDITION,
    DECLARATION,
    ELSE,
    ELSE_KEYWORD,
    EOF,
    FUNCTION,
    FUNCTION_NAME,
    // lambda, anonymous function
    FUNCTION_LITERAL,
    IDENTIFIER,
    IF,
    IF_KEYWORD,
    KEYWORD,
    LITERAL,
    LOOP,
    LOOP_FOREACH,
    STRING_LITERAL,
    BOOLEAN_LITERAL,
    PARAMETER,
    PARAMETER_LIST,
    OPERATOR,
    OPERATOR_ADD,
    OPERATOR_SUBTRACT,
    OPERATOR_MULTIPLY,
    OPERATOR_DIVIDE,
    OPERATOR_MODULO,
    OPERATOR_BINARY_AND,
    // Go: &^
    OPERATOR_BINARY_AND_NOT,
    OPERATOR_BINARY_OR,
    OPERATOR_BINARY_XOR,
    OPERATOR_LEFT_SHIFT,
    OPERATOR_RIGHT_SHIFT,
    OPERATOR_EQUAL,
    OPERATOR_LOGICAL_AND,
    OPERATOR_LOGICAL_OR,
    OPERATOR_NOT_EQUAL,
    OPERATOR_LESS_THAN,
    OPERATOR_LESS_OR_EQUAL,
    OPERATOR_GREATER_THAN,
    OPERATOR_GREATER_OR_EQUAL,
    RETURN,
    RESULT_LIST,
    STATEMENT,
    SWITCH,
    THEN,
    THROW,
    TYPE,
    UNSUPPORTED,;

    @Override
    public boolean test(UastNode uastNode) {
      return uastNode.kinds.contains(this);
    }
  }

  public Optional<UastNode> getChild(Kind kind) {
    return children.stream().filter(kind).findAny();
  }

  public List<UastNode> getChildren(Kind... kinds) {
    List<UastNode> selectedChildren = children.stream()
      .filter(child -> Arrays.stream(kinds).anyMatch(child.kinds::contains))
      .collect(Collectors.toList());
    return Collections.unmodifiableList(selectedChildren);
  }

  public void getDescendants(Kind kind, Consumer<UastNode> consumer, Kind... stopKinds) {
    if (Arrays.stream(stopKinds).noneMatch(kinds::contains)) {
      if (kinds.contains(kind)) {
        consumer.accept(this);
      }
      children.forEach(child -> child.getDescendants(kind, consumer, stopKinds));
    }
  }

  public void getDescendants(Kind kind, Consumer<UastNode> consumer) {
    if (kinds.contains(kind)) {
      consumer.accept(this);
    }
    children.forEach(child -> child.getDescendants(kind, consumer));
  }

  public boolean hasDescendant(Kind... kinds) {
    return is(kinds) || children.stream().anyMatch(child -> child.hasDescendant(kinds));
  }

  public Token firstToken() {
    if (token != null && !kinds.contains(Kind.COMMENT)) {
      return this.token;
    }
    for (UastNode child : children) {
      Token firstToken = child.firstToken();
      if (firstToken != null) {
        return firstToken;
      }
    }
    return null;
  }

  public Token lastToken() {
    if (token != null && !kinds.contains(Kind.COMMENT)) {
      return token;
    }
    ListIterator<UastNode> it = children.listIterator(children.size());
    while (it.hasPrevious()) {
      UastNode child = it.previous();
      Token lastToken = child.lastToken();
      if (lastToken != null) {
        return lastToken;
      }
    }
    return null;
  }

  public String joinTokens() {
    StringBuilder sb = new StringBuilder();
    SourcePos pos = kinds.contains(Kind.COMPILATION_UNIT) ? new SourcePos(1, 1) : new SourcePos(0, 0);
    joinTokens(sb, pos);
    return sb.toString();
  }

  private void joinTokens(StringBuilder sb, SourcePos pos) {
    if (token != null) {
      if (pos.line != 0) {
        while (pos.line < token.line) {
          sb.append('\n');
          pos.line++;
          pos.column = 1;
        }
        while (pos.column < token.column) {
          sb.append(' ');
          pos.column++;
        }
      }
      sb.append(token.value);
      pos.line = token.endLine;
      pos.column = token.endColumn + 1;
    }
    for (UastNode child : children) {
      child.joinTokens(sb, pos);
    }
  }

  public boolean is(Kind... kinds) {
    for (Kind kind : kinds) {
      if (this.kinds.contains(kind)) {
        return true;
      }
    }
    return false;
  }

  public boolean isNot(Kind... kinds) {
    return !is(kinds);
  }

  @Override
  public String toString() {
    if (token != null) {
      return token.toString();
    }
    Token firstToken = firstToken();
    return kinds.toString() + (firstToken != null ? firstToken.value : "");
  }

  private static class SourcePos {
    int line;
    int column;

    public SourcePos(int line, int column) {
      this.line = line;
      this.column = column;
    }
  }

}
