/*
 * SonarQube Go Plugin
 * Copyright (C) 2018-2018 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
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
