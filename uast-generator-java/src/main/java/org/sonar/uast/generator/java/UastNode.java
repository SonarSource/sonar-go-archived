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

  enum Kind {
    ASSIGNMENT,
    ASSIGNMENT_OPERATOR,
    ASSIGNMENT_TARGET,
    ASSIGNMENT_VALUE,
    BLOCK,
    BINARY_EXPRESSION,
    CASE,
    CLASS,
    COMPILATION_UNIT,
    CONDITION,
    DEFAULT_CASE,
    ELSE,
    EOF,
    FUNCTION,
    FUNCTION_LITERAL,
    IDENTIFIER,
    IF,
    LITERAL,
    PARAMETER,
    STATEMENT,
    SWITCH,
    COMMENT;
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

