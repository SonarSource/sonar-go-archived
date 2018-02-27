package org.sonar.uast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class UastNode {

  public Set<Kind> kinds = new HashSet<>();
  public List<UastNode> children = new ArrayList<>();
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

  public enum Kind {
    COMPILATION_UNIT,
    CLASS,
    FUNCTION
  }

  @Override
  public String toString() {
    if (token != null) {
      return token.toString();
    }
    return kinds.toString();
  }
}
