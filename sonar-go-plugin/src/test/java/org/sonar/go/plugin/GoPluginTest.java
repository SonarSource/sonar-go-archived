package org.sonar.go.plugin;

import org.junit.jupiter.api.Test;
import org.sonar.api.Plugin;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.SonarRuntime;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.utils.Version;

import static org.assertj.core.api.Assertions.assertThat;

class GoPluginTest {

  @Test
  void count_extension_points() {
    SonarRuntime runtime = SonarRuntimeImpl.forSonarQube(Version.create(6, 7), SonarQubeSide.SCANNER);
    Plugin.Context context = new Plugin.Context(runtime);
    Plugin underTest = new GoPlugin();
    underTest.define(context);
    assertThat(context.getExtensions()).hasSize(5);
  }
}
