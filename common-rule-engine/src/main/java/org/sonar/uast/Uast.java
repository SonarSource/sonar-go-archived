package org.sonar.uast;

import com.google.gson.Gson;
import java.io.Reader;

public class Uast {

  private static final Gson GSON = new Gson();

  private Uast() {
    // utility class
  }

  public static UastNode from(Reader reader) {
    return GSON.fromJson(reader, UastNode.class);
  }
}
