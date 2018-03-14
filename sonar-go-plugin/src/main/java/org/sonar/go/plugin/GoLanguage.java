package org.sonar.go.plugin;

import org.sonar.api.resources.AbstractLanguage;

public class GoLanguage extends AbstractLanguage {

  public static final String KEY = "go";

  public GoLanguage() {
    super(KEY, "Go");
  }

  @Override
  public String[] getFileSuffixes() {
    return new String[]{".go"};
  }
}
