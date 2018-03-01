package org.sonar.uast;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;

public final class UastNode {

  public final Set<Kind> kinds;
  public final String nativeNode;
  @Nullable
  public final Token token;
  public final List<UastNode> children;

  public UastNode(Set<Kind> kinds, String nativeNode, Token token, List<UastNode> children) {
    this.kinds = kinds;
    this.nativeNode = nativeNode;
    this.token = token;
    this.children = children;
  }

  public static class Token {
    public final String value;
    public final int line;
    public final int column;

    public Token(int line, int column, String value) {
      this.line = line;
      this.column = column;
      this.value = value;
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
    CLASS,
    COMPILATION_UNIT,
    FUNCTION,
    IDENTIFIER,
    EOF
    ;

    @Override
    public boolean test(UastNode uastNode) {
      return uastNode.kinds.contains(this);
    }
  }

  public Optional<UastNode> getChild(Kind kind) {
    return children.stream().filter(kind).findAny();
  }

  public UastNode firstToken() {
    if (token != null) {
      return this;
    }
    for (UastNode child : children) {
      UastNode uastNode = child.firstToken();
      if (uastNode != null) {
        return uastNode;
      }
    }
    return null;
  }

  @Override
  public String toString() {
    if (token != null) {
      return token.toString();
    }
    UastNode firstToken = firstToken();
    return kinds.toString() + (firstToken != null ? firstToken.toString() : "");
  }
}
