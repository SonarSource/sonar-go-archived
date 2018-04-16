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
import java.util.stream.Stream;
import javax.annotation.Nullable;

public final class UastNode {

  public final Set<Kind> kinds;
  public final String nativeNode;
  @Nullable
  public final Token token;
  public final List<UastNode> children;

  public UastNode(Set<Kind> kinds, String nativeNode, @Nullable Token token, List<UastNode> children) {
    this.kinds = kinds.stream().flatMap(Kind::kindAndExtendedKindStream).collect(Collectors.toSet());
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
    CONDITIONAL_JUMP,
    UNCONDITIONAL_JUMP,
    EXPRESSION,
    LABEL,
    BINARY_EXPRESSION(EXPRESSION),
    LEFT_OPERAND(EXPRESSION),
    RIGHT_OPERAND(EXPRESSION),
    PARENTHESIZED_EXPRESSION,
    ARRAY_ACCESS_EXPRESSION,
    ARRAY_OBJECT_EXPRESSION,
    ARRAY_KEY_EXPRESSION,
    LEFT_PARENTHESIS,
    RIGHT_PARENTHESIS,
    BLOCK,
    BODY,
    GOTO(UNCONDITIONAL_JUMP),
    BRANCH_LABEL,
    BREAK(UNCONDITIONAL_JUMP),
    CASE,
    CLASS,
    COMMENT,
    CONTINUE(UNCONDITIONAL_JUMP),
    DEFAULT_CASE,
    STRUCTURED_COMMENT(COMMENT),
    PACKAGE,
    CALL,
    MEMBER_SELECT,
    FALLTHROUGH(UNCONDITIONAL_JUMP),
    COMPILATION_UNIT,
    CONDITION,
    ELSE,
    ELSE_KEYWORD,
    EOF,
    FUNCTION,
    FUNCTION_NAME,
    VARIABLE_DECLARATION,
    CONSTANT_DECLARATION(VARIABLE_DECLARATION),
    VARIABLE_NAME,
    IMPORT,
    IMPORT_ENTRY,
    // lambda, anonymous function
    FUNCTION_LITERAL(EXPRESSION),
    IDENTIFIER,
    IF(CONDITIONAL_JUMP),
    IF_KEYWORD,
    KEYWORD,
    LITERAL,
    FLOAT_LITERAL(LITERAL),
    INT_LITERAL(LITERAL),
    DECIMAL_LITERAL(LITERAL),
    HEX_LITERAL(LITERAL),
    OCTAL_LITERAL(LITERAL),
    BINARY_LITERAL(LITERAL),
    STRING_LITERAL(LITERAL),
    CHAR_LITERAL(LITERAL),
    BOOLEAN_LITERAL(LITERAL),
    NULL_LITERAL(LITERAL),
    LOOP(CONDITIONAL_JUMP),
    FOR(LOOP),
    FOR_KEYWORD,
    FOR_INIT,
    FOR_UPDATE,
    FOREACH(LOOP),
    PARAMETER(VARIABLE_DECLARATION),
    PARAMETER_LIST,
    OPERATOR,
    ADD(BINARY_EXPRESSION),
    SUBTRACT(BINARY_EXPRESSION),
    MULTIPLY(BINARY_EXPRESSION),
    DIVIDE(BINARY_EXPRESSION),
    REMAINDER(BINARY_EXPRESSION),
    BITWISE_AND(BINARY_EXPRESSION),
    // Go: &^
    BITWISE_AND_NOT(BINARY_EXPRESSION),
    BITWISE_OR(BINARY_EXPRESSION),
    BITWISE_XOR(BINARY_EXPRESSION),
    LEFT_SHIFT(BINARY_EXPRESSION),
    RIGHT_SHIFT(BINARY_EXPRESSION),
    EQUAL(BINARY_EXPRESSION),
    LOGICAL_AND(BINARY_EXPRESSION),
    LOGICAL_OR(BINARY_EXPRESSION),
    NOT_EQUAL(BINARY_EXPRESSION),
    LESS_THAN(BINARY_EXPRESSION),
    LESS_OR_EQUAL(BINARY_EXPRESSION),
    GREATER_THAN(BINARY_EXPRESSION),
    GREATER_OR_EQUAL(BINARY_EXPRESSION),
    RETURN(UNCONDITIONAL_JUMP),
    RESULT_LIST,
    STATEMENT,
    EMPTY_STATEMENT(STATEMENT),
    SWITCH(CONDITIONAL_JUMP),
    THEN,
    THROW(UNCONDITIONAL_JUMP),
    TYPE,
    UNSUPPORTED,
    // Assignment
    ASSIGNMENT,
    ASSIGNMENT_OPERATOR,
    ASSIGNMENT_TARGET_LIST,
    ASSIGNMENT_TARGET,
    ASSIGNMENT_VALUE_LIST,
    ASSIGNMENT_VALUE,
    // Compound assignments
    COMPOUND_ASSIGNMENT(ASSIGNMENT),
    PLUS_ASSIGNMENT(ASSIGNMENT),
    MINUS_ASSIGNMENT(ASSIGNMENT),
    MULTIPLY_ASSIGNMENT(ASSIGNMENT),
    OR_ASSIGNMENT(ASSIGNMENT),
    AND_ASSIGNMENT(ASSIGNMENT),
    // Go: &^=
    AND_NOT_ASSIGNMENT(ASSIGNMENT),
    XOR_ASSIGNMENT(ASSIGNMENT),
    DIVIDE_ASSIGNMENT(ASSIGNMENT),
    REMAINDER_ASSIGNMENT(ASSIGNMENT),
    LEFT_SHIFT_ASSIGNMENT(ASSIGNMENT),
    RIGHT_SHIFT_ASSIGNMENT(ASSIGNMENT),
    // Java: <<<=
    UNSIGNED_RIGHT_SHIFT_ASSIGNMENT,
    // Unary expressions
    UNARY_EXPRESSION,
    OPERAND,
    UNARY_MINUS(UNARY_EXPRESSION),
    UNARY_PLUS(UNARY_EXPRESSION),
    POSTFIX_DECREMENT(UNARY_EXPRESSION),
    POSTFIX_INCREMENT(UNARY_EXPRESSION),
    PREFIX_DECREMENT(UNARY_EXPRESSION),
    PREFIX_INCREMENT(UNARY_EXPRESSION),
    LOGICAL_COMPLEMENT(UNARY_EXPRESSION),
    BITWISE_COMPLEMENT(UNARY_EXPRESSION),
    // Go: *
    POINTER(UNARY_EXPRESSION),
    // Go: &
    REFERENCE(UNARY_EXPRESSION),
    // Go; <-
    CHANNEL_DIRECTION(UNARY_EXPRESSION),
    ANNOTATION,
    ANNOTATION_TYPE,
    ARGUMENTS,
    ARGUMENT,
    ASSERT,
    CATCH,
    CONDITIONAL_EXPRESSION,
    TYPE_ARGUMENTS,
    TYPE_ARGUMENT,
    TYPE_PARAMETERS,
    TYPE_PARAMETER,
    WHILE(LOOP),
    DO_WHILE(LOOP),
    CAST,
    ENUM,
    INITIALIZER,
    TYPE_TEST,
    TRY(CONDITIONAL_JUMP),
    ;

    private final Set<Kind> extendedKinds;

    Kind() {
      this.extendedKinds = Collections.emptySet();
    }

    Kind(Kind... extendedKinds) {
      this.extendedKinds = Arrays.stream(extendedKinds).flatMap(Kind::kindAndExtendedKindStream)
        .collect(Collectors.toSet());
    }

    public Set<Kind> extendedKinds() {
      return extendedKinds;
    }

    public Stream<Kind> kindAndExtendedKindStream() {
      if (extendedKinds.isEmpty()) {
        return Stream.of(this);
      }
      return Stream.concat(Stream.of(this), extendedKinds.stream());
    }

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
