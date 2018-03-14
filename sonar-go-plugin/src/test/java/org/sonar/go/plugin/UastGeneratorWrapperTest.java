package org.sonar.go.plugin;

import java.io.File;
import java.nio.charset.StandardCharsets;
import org.assertj.core.util.Files;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.uast.UastNode;

import static org.assertj.core.api.Assertions.assertThat;

class UastGeneratorWrapperTest {

  private File workDir;

  @BeforeEach
  void setUp() {
    workDir = Files.temporaryFolder();
    workDir.deleteOnExit();
  }

  @Test
  void test() throws Exception {
    UastGeneratorWrapper generator = new UastGeneratorWrapper(workDir);

    InputFile inputFile = TestInputFileBuilder.create("module", "foo.go")
      .setLanguage(GoLanguage.KEY)
      .setCharset(StandardCharsets.UTF_8)
      .setContents("package main\n" +
        "func foo() {}")
      .build();
    SensorContextTester sensorContext = SensorContextTester.create(workDir);
    sensorContext.fileSystem().add(inputFile);

    UastNode uast = generator.createUast(inputFile);
    assertThat(uast.joinTokens()).isEqualTo("package main\n" +
      "func foo() {}");
  }
}
