package org.sonar.go.plugin.utils;

import java.io.InputStream;
import java.util.Scanner;

public class TestUtils {

  private TestUtils() {
    // utility class, forbidden constructor
  }

  public static String readTestResource(Class<?> clazz, String filename) {
    String resource = clazz.getSimpleName() + "/" + filename;
    InputStream resourceAsStream = clazz.getResourceAsStream(resource);
    if (resourceAsStream == null) {
      throw new IllegalStateException("Resource " + resource + " not found on classpath.");
    }
    Scanner scanner = new java.util.Scanner(resourceAsStream).useDelimiter("\\A");
    return scanner.hasNext() ? scanner.next() : "";
  }
}
