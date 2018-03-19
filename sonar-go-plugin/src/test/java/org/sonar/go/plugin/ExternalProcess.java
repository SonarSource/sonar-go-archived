package org.sonar.go.plugin;

import java.util.Arrays;

/**
 * Used to test external process return values
 */
public class ExternalProcess {

  public static void main(String[] args) {
    System.err.println(Arrays.toString(args));
    System.out.println(args[0]);
    System.exit(Integer.valueOf(args[1]));
  }
}
