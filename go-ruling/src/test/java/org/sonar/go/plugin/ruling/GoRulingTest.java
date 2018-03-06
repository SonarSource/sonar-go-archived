package org.sonar.go.plugin.ruling;

import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.sonar.commonruleengine.Engine;
import org.sonar.commonruleengine.Issue;
import org.sonar.uast.Uast;
import org.sonar.uast.UastNode;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GoRulingTest {

  public static final Path GO_SOURCE_DIRECTORY = Paths.get("src", "test", "resources", "go");
  public static final Path GO_EXPECTED_DIRECTORY = GO_SOURCE_DIRECTORY.resolve("expected");
  public static final Path GO_ACTUAL_DIRECTORY = Paths.get("build", "tmp", "actual", "go");

  @Test
  void ruling() throws IOException {
    Files.createDirectories(GO_ACTUAL_DIRECTORY);
    Map<String, Map<String, List<String>>> issuesPreRulePreFile = new TreeMap<>();
    try (Stream<Path> files = Files.walk(GO_SOURCE_DIRECTORY)) {
      files.filter(path -> path.toString().endsWith(".go"))
        .sorted()
        .forEach(path -> analyze(issuesPreRulePreFile, path));
    }
    StringBuilder expected = new StringBuilder();
    StringBuilder actual = new StringBuilder();
    for (Map.Entry<String, Map<String, List<String>>> preRulePreFileEntry : issuesPreRulePreFile.entrySet()) {
      String ruleName = preRulePreFileEntry.getKey();
      expected.append("[").append(ruleName).append("]\n");
      expected.append(getExpected(ruleName)).append("\n");
      actual.append("[").append(ruleName).append("]\n");
      actual.append(getAndWriteActual(ruleName, preRulePreFileEntry.getValue())).append("\n");
    }
    assertEquals(expected.toString(), actual.toString());
  }

  private String getAndWriteActual(String ruleName, Map<String, List<String>> issuesPreFile) throws IOException {
    StringBuilder actual = new StringBuilder();
    for (Map.Entry<String, List<String>> issuesPreFileEntry : issuesPreFile.entrySet()) {
      String fileName = issuesPreFileEntry.getKey();
      actual.append(fileName).append(":");
      issuesPreFileEntry.getValue().forEach(actual::append);
      actual.append("\n");
    }
    String actualContent = actual.toString();
    Path actualFile = GO_ACTUAL_DIRECTORY.resolve(ruleName + ".txt");
    if (actualContent.isEmpty()) {
      Files.deleteIfExists(actualFile);
    } else {
      Files.write(actualFile, actualContent.getBytes(UTF_8));
    }
    return actualContent;
  }

  String getExpected(String ruleName) throws IOException {
    Path expectedFile = GO_EXPECTED_DIRECTORY.resolve(ruleName + ".txt");
    if (Files.exists(expectedFile)) {
      return new String(Files.readAllBytes(expectedFile), UTF_8);
    } else {
      return "";
    }
  }

  void analyze(Map<String, Map<String, List<String>>> issuesPreRulePreFile, Path path) {
    try {
      UastNode uast = getGoUast(path);
      Engine engine = new Engine(Engine.ALL_CHECKS);
      for (Issue issue : engine.scan(uast)) {
        String ruleName = issue.getRule().getClass().getSimpleName();
        String filename = GO_SOURCE_DIRECTORY.relativize(path).toString().replace('\\', '/');
        issuesPreRulePreFile
          .computeIfAbsent(ruleName, key -> new TreeMap<>())
          .computeIfAbsent(filename, key -> new ArrayList<>())
          .add(" " + getNodeLocation(issue));
      }
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  private String getNodeLocation(Issue issue) {
    UastNode node = issue.getNode().firstToken();
    if (node != null) {
      UastNode.Token token = node.token;
      if (token != null) {
        return token.line + ":" + token.column;
      }
    }
    return "unknown";
  }

  private UastNode getGoUast(Path path) throws IOException {
    ProcessBuilder builder = new ProcessBuilder(goParserPath(), path.toAbsolutePath().toString());
    builder.redirectErrorStream(true);
    Process process = builder.start();
    try (InputStream inputStream = process.getInputStream()) {
      String jsonOrError = CharStreams.toString(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
      if (!jsonOrError.startsWith("{")) {
        throw new IllegalArgumentException("Invalid file " + path + " :\n" + jsonOrError);
      }
      return Uast.from(new StringReader(jsonOrError));
    }
  }

  String goParserPath() {
    String name;
    String os = System.getProperty("os.name").toLowerCase();
    if (os.contains("win")) {
      name = "uast-generator-go-windows-amd64.exe";
    } else if (os.contains("mac")) {
      name = "uast-generator-go-darwin-amd64";
    } else {
      name = "uast-generator-go-linux-amd64";
    }
    return Paths.get("..", "uast-generator-go", "build", name).toAbsolutePath().normalize().toString();
  }
}
