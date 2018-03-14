package org.sonar.go.plugin;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GoLanguageTest {

  @Test
  public void should_have_correct_file_extensions() throws Exception {
    GoLanguage typeScriptLanguage = new GoLanguage();
    assertThat(typeScriptLanguage.getFileSuffixes()).containsExactly(".go");
  }
}
