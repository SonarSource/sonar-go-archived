package org.sonar.go.plugin;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.assertj.core.util.Files;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.uast.UastNode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UastGeneratorWrapperTest {

  private File workDir;

  @BeforeEach
  void setUp() {
    workDir = Files.temporaryFolder();
    workDir.deleteOnExit();
  }

  @Test
  void test() throws Exception {
    SensorContextTester sensorContext = SensorContextTester.create(workDir);
    sensorContext.fileSystem().setWorkDir(workDir.toPath());
    UastGeneratorWrapper generator = new UastGeneratorWrapper(sensorContext);
    ByteArrayInputStream in = new ByteArrayInputStream("package main\nfunc foo() {}".getBytes(StandardCharsets.UTF_8));

    UastNode uast = generator.createUast(in);
    assertThat(uast.joinTokens()).isEqualTo("package main\n" +
      "func foo() {}");
  }

  @Test
  void test_non_zero_return_value() throws Exception {
    String cp = System.getProperty("java.class.path");
    UastGeneratorWrapper generator = new UastGeneratorWrapper(() -> Arrays.asList("java", "-cp", cp, ExternalProcess.class.getCanonicalName(), "{kinds: []}", "2"));

    assertThatThrownBy(() -> generator.createUast(new ByteArrayInputStream(new byte[] {})))
      .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void test_stuck_process() throws Exception {
    String cp = System.getProperty("java.class.path");
    UastGeneratorWrapper generator = new UastGeneratorWrapper(() -> Arrays.asList("java", "-cp", cp, StuckExternalProcess.class.getCanonicalName(), "{kinds: []}"));

    assertThatThrownBy(() -> generator.createUast(new ByteArrayInputStream(new byte[] {})))
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("Took too long to parse. External process killed forcibly");
  }


}
