package org.sonar.go.plugin;

import org.sonar.api.Plugin;

public class GoPlugin implements Plugin {


  @Override
  public void define(Context context) {
    context.addExtensions(GoLanguage.class, GoSensor.class, SonarWayProfile.class);
  }
}
