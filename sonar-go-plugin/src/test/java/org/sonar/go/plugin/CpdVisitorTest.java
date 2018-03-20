package org.sonar.go.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.duplications.internal.pmd.TokensLine;
import org.sonar.uast.Uast;
import org.sonar.uast.UastNode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.go.plugin.utils.TestUtils.readTestResource;

class CpdVisitorTest {

  private Path workDir;
  private Path projectDir;
  private SensorContextTester sensorContext;

  @BeforeEach
  void setUp() throws IOException {
    workDir = Files.createTempDirectory("gotest");
    workDir.toFile().deleteOnExit();
    projectDir = Files.createTempDirectory("gotestProject");
    projectDir.toFile().deleteOnExit();
    sensorContext = SensorContextTester.create(workDir);
    sensorContext.fileSystem().setWorkDir(workDir);
  }

  @Test
  void test() throws IOException {
    String filename = "lets.go";
    String code = readTestResource(getClass(), filename);
    String codeJson = readTestResource(getClass(), filename + ".uast.json");

    InputFile inputFile = createInputFile("lets.go", code);

    sensorContext.fileSystem().add(inputFile);
    CpdVisitor cpdVisitor = new CpdVisitor(sensorContext, inputFile);

    UastNode node = Uast.from(new StringReader(codeJson));
    cpdVisitor.scan(node);
    cpdVisitor.save();

    List<TokensLine> tokensLines = sensorContext.cpdTokens("module:" + inputFile.filename());
    assertThat(tokensLines).isNotNull().hasSize(5);
    assertThat(tokensLines).extracting("value").isEqualTo(Arrays.asList(
      "packagemain",
      "funcfun()string{",
      "a:=LITERAL",
      "returna",
      "}"));
    assertThat(tokensLines).extracting("startLine").isEqualTo(Arrays.asList(1, 3, 4, 5, 6));
  }

  private InputFile createInputFile(String filename, String content) {
    Path filePath = projectDir.resolve(filename);
    return TestInputFileBuilder.create("module", projectDir.toFile(), filePath.toFile())
      .setCharset(StandardCharsets.UTF_8)
      .setContents(content)
      .build();
  }
}
