package org.sonar.uast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UastNode {

  public Set<String> kinds = new HashSet<>();
  public List<UastNode> children = new ArrayList<>();
  public Position position;
  public String value;
  public String nativeNode;

  public static class Position {
    public int line;
    public int column;
  }


}
