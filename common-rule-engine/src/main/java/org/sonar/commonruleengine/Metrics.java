package org.sonar.commonruleengine;

import java.util.HashSet;
import java.util.Set;

public class Metrics {
  public int numberOfClasses = 0;
  public int numberOfFunctions = 0;
  public int numberOfStatements = 0;

  public Set<Integer> linesOfCode = new HashSet<>();
  public Set<Integer> commentLines = new HashSet<>();
}
