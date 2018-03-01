package org.sonar.uast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import java.io.Reader;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

public final class Uast {

  private static final JsonDeserializer<UastNode> NODE_DESERIALIZER = (json, type, context) -> {
    JsonUastNode node = context.deserialize(json, JsonUastNode.class);
    if (node == null) {
      return null;
    }
    if (node.kinds != null) {
      // unsupported enum values are replaced by null
      node.kinds.remove(null);
    }
    return new UastNode(
      node.kinds != null ? node.kinds : Collections.emptySet(),
      node.nativeNode != null ? node.nativeNode : "",
      node.token,
      node.children != null ? node.children : Collections.emptyList());
  };

  private static final JsonDeserializer<UastNode.Token> TOKEN_DESERIALIZER = (json, type, context) -> {
    JsonUastToken token = context.deserialize(json, JsonUastToken.class);
    if (token == null) {
      return null;
    }
    return new UastNode.Token(
      token.line,
      token.column,
      token.value != null ? token.value : ""

    );
  };

  private static final Gson GSON = new GsonBuilder()
    .registerTypeAdapter(UastNode.class, NODE_DESERIALIZER)
    .registerTypeAdapter(UastNode.Token.class, TOKEN_DESERIALIZER)
    .create();

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

  public static boolean syntacticallyEquivalent(List<UastNode> node1, List<UastNode> node2) {
    if (node1.size() != node2.size()) {
      return false;
    }
    Iterator<UastNode> it1 = node1.iterator();
    Iterator<UastNode> it2 = node2.iterator();
    while (it1.hasNext()) {
      if (!syntacticallyEquivalent(it1.next(), it2.next())) {
        return false;
      }
    }
    return true;
  }

  private static class JsonUastNode {
    @Nullable
    Set<UastNode.Kind> kinds;
    @Nullable
    String nativeNode;
    @Nullable
    UastNode.Token token;
    @Nullable
    List<UastNode> children;
 }

  private static class JsonUastToken {
    int line;
    int column;
    @Nullable
    String value;
  }
}
