package org.sonar.commonruleengine.checks;

import java.io.InputStreamReader;
import org.sonar.uast.Uast;
import org.sonar.uast.UastNode;

public class Utils {

  public Utils() {
    // utility class
  }

  static UastNode fromClasspath(String resource) {
    return Uast.from(new InputStreamReader(Utils.class.getResourceAsStream(resource)));
  }
}
