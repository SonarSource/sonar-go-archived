package org.sonar.uast;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;

public final class UastNode {

  public Set<Kind> kinds = Collections.emptySet();
  public List<UastNode> children = Collections.emptyList();
  @Nullable
  public Token token;
  public String nativeNode;

  public static class Token {
    public String value;
    public int line;
    public int column;

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
    ;

    @Override
    public boolean test(UastNode uastNode) {
      return uastNode.kinds.contains(this);
    }
  }

  public Optional<UastNode> getChild(Kind kind) {
    return children.stream().filter(kind).findAny();
  }

  @Override
  public String toString() {
    if (token != null) {
      return token.toString();
    }
    return kinds.toString();
  }
}
