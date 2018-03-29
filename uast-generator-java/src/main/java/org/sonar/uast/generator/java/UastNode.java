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
package org.sonar.uast.generator.java;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
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
    ASSIGNMENT,
    ASSIGNMENT_OPERATOR,
    ASSIGNMENT_TARGET,
    ASSIGNMENT_VALUE,
    EXPRESSION,
    LABEL,
    BINARY_EXPRESSION,
    PARENTHESIZED_EXPRESSION,
    LEFT_PARENTHESIS("("),
    RIGHT_PARENTHESIS(")"),
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
    ELSE_KEYWORD("else", KEYWORD),
    EOF,
    FUNCTION,
    FUNCTION_NAME,
    // lambda, anonymous function
    FUNCTION_LITERAL,
    IDENTIFIER,
    IF,
    IF_KEYWORD("if", KEYWORD),
    LITERAL,
    LOOP,
    LOOP_FOREACH,
    STRING_LITERAL,
    BOOLEAN_LITERAL,
    PARAMETER,
    OPERATOR,
    OPERATOR_ADD("+", OPERATOR),
    OPERATOR_SUBTRACT("-", OPERATOR),
    OPERATOR_MULTIPLY("*", OPERATOR),
    OPERATOR_DIVIDE("/", OPERATOR),
    OPERATOR_MODULO("%", OPERATOR),
    OPERATOR_BINARY_AND("&", OPERATOR),
    OPERATOR_BINARY_OR("|", OPERATOR),
    OPERATOR_BINARY_XOR("^", OPERATOR),
    OPERATOR_LEFT_SHIFT("<<", OPERATOR),
    OPERATOR_RIGHT_SHIFT(">>", OPERATOR),
    OPERATOR_EQUAL("==", OPERATOR),
    OPERATOR_LOGICAL_AND("&&", OPERATOR),
    OPERATOR_LOGICAL_OR("||", OPERATOR),
    OPERATOR_NOT_EQUAL("!=", OPERATOR),
    OPERATOR_LESS_THAN("<", OPERATOR),
    OPERATOR_LESS_OR_EQUAL("<=", OPERATOR),
    OPERATOR_GREATER_THAN(">", OPERATOR),
    OPERATOR_GREATER_OR_EQUAL(">=", OPERATOR),
    RETURN,
    STATEMENT,
    SWITCH,
    THEN,
    THROW,
    TYPE,;

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
    int column;

    public Token(int line, int column, String value) {
      this.value = value;
      this.line = line;
      this.column = column;
    }
  }
}

