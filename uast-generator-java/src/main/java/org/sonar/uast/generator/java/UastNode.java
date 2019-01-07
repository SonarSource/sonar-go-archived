/*
 * SonarQube Go Plugin
 * Copyright (C) 2018-2019 SonarSource SA
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
package org.sonar.uast.generator.java;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

/**
 * This class copies structure of {@link org.sonar.uast.UastNode}, but we don't want dependency on common-rule-engine project
 */
class UastNode {
  Set<Kind> kinds;
  String nativeNode;
  Token token;
  List<UastNode> children;

  public UastNode(Set<Kind> kinds, String nativeNode, @Nullable Token token, List<UastNode> children) {
    this.kinds = kinds;
    this.nativeNode = nativeNode;
    this.token = token;
    this.children = children;
  }

  // this enum is copy&paste from common-rule-engine
  enum Kind {
    KEYWORD,
    EXPRESSION,
    LABEL,
    BINARY_EXPRESSION,
    LEFT_OPERAND,
    RIGHT_OPERAND,
    PARENTHESIZED_EXPRESSION,
    LEFT_PARENTHESIS("("),
    RIGHT_PARENTHESIS(")"),
    BLOCK,
    BRANCH_LABEL,
    BREAK,
    CASE,
    CLASS,
    COMMENT,
    CONTINUE,
    DEFAULT_CASE,
    STRUCTURED_COMMENT,
    COMPILATION_UNIT,
    CONDITION,
    ELSE,
    ELSE_KEYWORD("else", KEYWORD),
    EOF,
    CONSTRUCTOR,
    FUNCTION,
    FUNCTION_NAME,
    // lambda, anonymous function
    FUNCTION_LITERAL,
    IDENTIFIER,
    IF,
    IF_KEYWORD("if", KEYWORD),
    FOREACH,
    FOR,
    FOR_KEYWORD,
    FOR_INIT,
    FOR_UPDATE,
    BODY,
    STRING_LITERAL,
    BOOLEAN_LITERAL,
    HEX_LITERAL,
    FLOAT_LITERAL,
    DECIMAL_LITERAL,
    OCTAL_LITERAL,
    BINARY_LITERAL,
    NULL_LITERAL,
    PARAMETER_LIST,
    PARAMETER,
    OPERATOR,
    ADD,
    SUBTRACT,
    MULTIPLY,
    DIVIDE,
    REMAINDER,
    BITWISE_AND,
    BITWISE_OR,
    BITWISE_XOR,
    LEFT_SHIFT,
    RIGHT_SHIFT,
    EQUAL,
    LOGICAL_AND,
    LOGICAL_OR,
    NOT_EQUAL,
    LESS_THAN,
    LESS_OR_EQUAL,
    GREATER_THAN,
    GREATER_OR_EQUAL,
    RETURN,
    RESULT_LIST,
    STATEMENT,
    SWITCH,
    THEN,
    THROW,
    TYPE,
    ASSIGNMENT,
    ASSIGNMENT_OPERATOR,
    ASSIGNMENT_TARGET,
    ASSIGNMENT_VALUE,
    PLUS_ASSIGNMENT,
    MINUS_ASSIGNMENT,
    MULTIPLY_ASSIGNMENT,
    OR_ASSIGNMENT,
    AND_ASSIGNMENT,
    XOR_ASSIGNMENT,
    DIVIDE_ASSIGNMENT,
    REMAINDER_ASSIGNMENT,
    LEFT_SHIFT_ASSIGNMENT,
    RIGHT_SHIFT_ASSIGNMENT,
    UNSIGNED_RIGHT_SHIFT_ASSIGNMENT,
    UNARY_EXPRESSION,
    OPERAND,
    UNARY_MINUS,
    UNARY_PLUS,
    POSTFIX_DECREMENT,
    POSTFIX_INCREMENT,
    PREFIX_DECREMENT,
    PREFIX_INCREMENT,
    LOGICAL_COMPLEMENT,
    BITWISE_COMPLEMENT,
    ANNOTATION,
    ANNOTATION_TYPE,
    ARGUMENTS,
    CATCH,
    CONDITIONAL_EXPRESSION,
    ARGUMENT,
    ASSERT,
    EMPTY_STATEMENT,
    TYPE_ARGUMENTS,
    TYPE_ARGUMENT,
    TYPE_PARAMETERS,
    TYPE_PARAMETER,
    CHAR_LITERAL,
    DO_WHILE,
    WHILE,
    VARIABLE_DECLARATION,
    VARIABLE_NAME,
    ARRAY_ACCESS_EXPRESSION,
    ARRAY_OBJECT_EXPRESSION,
    ARRAY_KEY_EXPRESSION,
    CAST,
    ENUM,
    CONSTANT_DECLARATION,
    IMPORT,
    IMPORT_ENTRY,
    INITIALIZER,
    TYPE_TEST,
    MEMBER_SELECT,
    CALL,
    PACKAGE,
    TRY;

    @Nullable
    final String token;
    final List<Kind> impliedKinds;

    Kind() {
      this.token = null;
      impliedKinds = Collections.emptyList();
    }

    Kind(String token, Kind... impliedKinds) {
      this.token = token;
      this.impliedKinds = Arrays.asList(impliedKinds);
    }

    boolean isKindForToken(String token) {
      return token.equals(this.token);
    }
  }

  static class Token {
    String value;
    int line;
    int endLine;
    int column;
    int endColumn;

    // copy-pasted from org.sonar.uast.UastNode.Token
    private static final Pattern LINE_SPLITTER = Pattern.compile("\r\n|\n|\r");

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
      return "[" + line + ":" + column + "] " + value;
    }
  }
}

