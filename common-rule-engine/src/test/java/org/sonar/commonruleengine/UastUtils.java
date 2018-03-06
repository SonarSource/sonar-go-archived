package org.sonar.commonruleengine;

import java.io.InputStream;
import java.io.InputStreamReader;
import org.sonar.uast.Uast;
import org.sonar.uast.UastNode;

public class UastUtils {

  private UastUtils() {
    // utility class, forbidden constructor
  }

  public static UastNode fromClasspath(Class<?> clazz, String resource) {
    InputStream resourceAsStream = clazz.getResourceAsStream(resource);
    if (resourceAsStream == null) {
      throw new IllegalStateException("Resource " + resource + " not found on classpath.");
    }
    return Uast.from(new InputStreamReader(resourceAsStream));
  }
}
