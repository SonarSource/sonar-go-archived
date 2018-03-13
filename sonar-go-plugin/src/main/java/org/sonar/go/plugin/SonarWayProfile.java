package org.sonar.go.plugin;

import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;

public class SonarWayProfile implements BuiltInQualityProfilesDefinition {

  @Override
  public void define(Context context) {
    JsonProfileDefinition profileDefinition = loadProfileDefinition("Sonar_way_profile.json");
    if (profileDefinition.ruleKeys.isEmpty()) {
      // basic sanity check
      throw new IllegalStateException("No rules defined in Sonar way profile");
    }
    NewBuiltInQualityProfile profile = context.createBuiltInQualityProfile(profileDefinition.name, GoLanguage.KEY);
    profile.setDefault(true);
    profileDefinition.ruleKeys.forEach(ruleKey -> profile.activateRule(GoRulesDefinition.REPOSITORY_KEY, ruleKey));
    profile.done();
  }

  private static JsonProfileDefinition loadProfileDefinition(String jsonFile) {
    InputStream resource = GoPlugin.class.getClassLoader().getResourceAsStream(GoPlugin.RESOURCE_FOLDER + "/" + jsonFile);
    if (resource == null) {
      throw new IllegalStateException(jsonFile + " not found");
    }
    return new Gson().fromJson(new InputStreamReader(resource, StandardCharsets.UTF_8), JsonProfileDefinition.class);
  }

  static class JsonProfileDefinition {
    String name;
    List<String> ruleKeys;
  }
}
