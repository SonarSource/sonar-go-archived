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
