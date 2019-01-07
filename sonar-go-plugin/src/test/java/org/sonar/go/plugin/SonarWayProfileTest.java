/*
 * SonarQube Go Plugin
 * Copyright (C) 2018-2019 SonarSource SA
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

import org.junit.jupiter.api.Test;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;
import org.sonar.api.utils.ValidationMessages;

import static org.assertj.core.api.Assertions.assertThat;

class SonarWayProfileTest {

  @Test
  public void should_create_sonar_way_profile() {
    ValidationMessages validation = ValidationMessages.create();

    BuiltInQualityProfilesDefinition.Context context = new BuiltInQualityProfilesDefinition.Context();
    new SonarWayProfile().define(context);

    assertThat(context.profilesByLanguageAndName()).hasSize(1);
    BuiltInQualityProfilesDefinition.BuiltInQualityProfile profile = context.profile("go", "Sonar way");

    assertThat(profile.language()).isEqualTo("go");
    assertThat(profile.name()).isEqualTo("Sonar way");
    assertThat(profile.rules()).extracting("repoKey").containsOnly(GoRulesDefinition.REPOSITORY_KEY);
    assertThat(validation.hasErrors()).isFalse();
    assertThat(profile.rules()).extracting("ruleKey").contains("S2068");
    assertThat(profile.rules()).extracting("ruleKey").doesNotContain("S3801");
  }

}
