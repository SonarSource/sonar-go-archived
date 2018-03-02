package org.sonar.go.plugin.ruling;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.sonar.commonruleengine.Engine;
import org.sonar.commonruleengine.Issue;
import org.sonar.uast.Uast;
import org.sonar.uast.UastNode;

import static java.nio.charset.StandardCharsets.UTF_8;

class GoRulingTest {

  @Test
  void ruling() throws IOException {
    StringBuilder actualResult = new StringBuilder();
    try (Stream<Path> files = Files.walk(Paths.get("..", "go-samples"))) {
      files.filter(path -> path.toString().endsWith(".go"))
        .sorted()
        .forEach(path -> analyze(actualResult, path));
    }
    Path expectedPath = Paths.get("src/test/resources/analyze-expected-result.txt");
    String expected = new String(Files.readAllBytes(expectedPath), UTF_8);
    Assertions.assertEquals(expected, actualResult.toString());
  }

  void analyze(StringBuilder out, Path path) {
    try {
      out.append("[" + path.toString().replace('\\', '/') + "]\n");
      UastNode uast = getGoUast(path);
      Engine engine = new Engine(Engine.ALL_CHECKS);
      List<Issue> issues = engine.scan(uast);
      out.append(issues.stream().map(Issue::toString).collect(Collectors.joining("\n")));
      out.append("\n");
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  private UastNode getGoUast(Path path) throws IOException {
    ProcessBuilder builder = new ProcessBuilder(goParserPath(), path.toAbsolutePath().toString());
    builder.redirectErrorStream(true);
    Process process = builder.start();
    try (InputStream inputStream = process.getInputStream()) {
      return Uast.from(new InputStreamReader(inputStream));
    }
  }

  String goParserPath() {
    String name;
    String os = System.getProperty("os.name").toLowerCase();
    if (os.contains("win")) {
      name = "goparser-windows-amd64.exe";
    } else if (os.contains("mac")) {
      name = "goparser-darwin-amd64";
    } else {
      name = "goparser-linux-amd64";
    }
    return Paths.get("..", "goparser", "build", name).toAbsolutePath().normalize().toString();
  }
}
