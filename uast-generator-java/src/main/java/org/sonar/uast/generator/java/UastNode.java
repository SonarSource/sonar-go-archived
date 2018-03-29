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
    OPERATOR,
    OPERATOR_ADD,
    OPERATOR_EQUAL,
    OPERATOR_LOGICAL_AND,
    OPERATOR_LOGICAL_OR,
    OPERATOR_MULTIPLY,
    OPERATOR_NOT_EQUAL,
    RETURN,
    STATEMENT,
    SWITCH,
    THEN,
    THROW,
    TYPE,
    ;
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

