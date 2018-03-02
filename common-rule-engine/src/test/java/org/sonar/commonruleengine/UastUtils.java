package org.sonar.commonruleengine;

import java.io.InputStreamReader;
import org.sonar.uast.Uast;
import org.sonar.uast.UastNode;

public class UastUtils {

  private UastUtils() {
    // utility class, forbidden constructor
  }

  public static UastNode fromClasspath(Class<?> clazz, String resource) {
    return Uast.from(new InputStreamReader(clazz.getResourceAsStream(resource)));
  }
}
