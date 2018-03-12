package org.sonar.commonruleengine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.sonar.uast.Uast;
import org.sonar.uast.UastNode;

public class UastUtils {

  private UastUtils() {
    // utility class, forbidden constructor
  }

  public static UastNode fromFile(File file) throws IOException {
    InputStream resourceAsStream;
    try {
      resourceAsStream = new FileInputStream(file);
    } catch (FileNotFoundException e) {
      throw new IllegalStateException("File " + file.getAbsolutePath() + " not found on classpath.");
    }
    return Uast.from(new InputStreamReader(resourceAsStream));
  }
}
