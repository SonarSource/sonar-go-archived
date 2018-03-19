package org.sonar.go.plugin;

/**
 * Used to test external process return values
 */
public class StuckExternalProcess {

  public static void main(String[] args) throws InterruptedException {
    System.out.println(args[0]);
    Thread.sleep(10_000);
  }
}
