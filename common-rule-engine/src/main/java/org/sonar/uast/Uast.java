package org.sonar.uast;

import com.google.gson.Gson;
import java.io.Reader;
import java.util.Iterator;

public final class Uast {

  private static final Gson GSON = new Gson();

  private Uast() {
    // utility class
  }

  public static UastNode from(Reader reader) {
    return GSON.fromJson(reader, UastNode.class);
  }

  public static boolean syntacticallyEquivalent(UastNode node1, UastNode node2) {
    if (node1.token == null && node2.token != null) {
      return false;
    }
    if (node2.token != null && !node1.token.value.equals(node2.token.value)) {
      return false;
    }
    if (node1.children.size() != node2.children.size()) {
      return false;
    }
    Iterator<UastNode> child1 = node1.children.iterator();
    Iterator<UastNode> child2 = node2.children.iterator();
    while (child1.hasNext()) {
      if (!syntacticallyEquivalent(child1.next(), child2.next())) {
        return false;
      }
    }
    return true;
  }
}
