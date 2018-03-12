package org.sonar.go.plugin;

import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;

public class SonarWayProfile implements BuiltInQualityProfilesDefinition {

  @Override
  public void define(Context context) {
    context.createBuiltInQualityProfile("Sonar way", GoLanguage.KEY).done();
  }
}
