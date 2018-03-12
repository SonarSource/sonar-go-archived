package org.sonar.go.plugin;

import org.sonar.api.Plugin;

public class GoPlugin implements Plugin {

  static final String RESOURCE_FOLDER = "org/sonar/l10n/go/rules/go";

  @Override
  public void define(Context context) {
    context.addExtensions(
      GoLanguage.class,
      GoSensor.class,
      GoRulesDefinition.class,
      SonarWayProfile.class);
  }
}
