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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.sonar.commonruleengine.Engine;
import org.sonar.commonruleengine.Issue;
import org.sonar.commonruleengine.Metrics;
import org.sonar.uast.Uast;
import org.sonar.uast.UastNode;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@EnabledIfEnvironmentVariable(named = "ruling", matches = "true")
class GoRulingTest {

  public static final Path GO_SOURCE_DIRECTORY = Paths.get("src", "test", "ruling-test-sources");
  public static final Path GO_EXPECTED_DIRECTORY = Paths.get("src", "test", "resources", "go", "expected");
  public static final Path GO_ACTUAL_DIRECTORY = Paths.get("build", "tmp", "actual", "go");

  @Test
  void ruling() throws IOException {
    if (!Files.exists(GO_SOURCE_DIRECTORY.resolve("README.md"))) {
      fail(GO_SOURCE_DIRECTORY + " submodule does not contains 'README.md'," +
        " you probably need to do a 'git submodule update --init'");
    }
    Files.createDirectories(GO_ACTUAL_DIRECTORY);
    Map<String, Map<String, List<String>>> issuesPreRulePreFile = new ConcurrentHashMap<>();
    try (Stream<Path> files = Files.walk(GO_SOURCE_DIRECTORY)) {
      files.filter(path -> path.toString().endsWith(".go"))
        .parallel()
        .forEach(path -> analyze(issuesPreRulePreFile, path));
    }
    StringBuilder expected = new StringBuilder();
    StringBuilder actual = new StringBuilder();
    List<Map.Entry<String, Map<String, List<String>>>> entries = new ArrayList<>(issuesPreRulePreFile.entrySet());
    entries.sort(Comparator.comparing(Map.Entry::getKey));
    for (Map.Entry<String, Map<String, List<String>>> preRulePreFileEntry : entries) {
      String ruleName = preRulePreFileEntry.getKey();
      expected.append("[").append(ruleName).append("]\n");
      expected.append(getExpected(ruleName)).append("\n");
      actual.append("[").append(ruleName).append("]\n");
      actual.append(getAndWriteActual(ruleName, preRulePreFileEntry.getValue())).append("\n");
    }
    assertEquals(expected.toString(), actual.toString());
  }

  @Test
  void metrics() {
    UastNode uast = getGoUast(GO_SOURCE_DIRECTORY.resolve("samples").resolve("SelfAssignement.go"));
    Engine engine = new Engine(Collections.emptyList());
    Metrics metrics = engine.scan(uast).metrics;
    // TODO should be 1
    assertEquals(0, metrics.numberOfClasses);
    assertEquals(1, metrics.numberOfFunctions);
    assertEquals(3, metrics.numberOfStatements);
    // TODO should be 2, 4, 6, 7, 8, 9, 10
    assertEquals(new HashSet<>(Arrays.asList(2, 6, 7, 8, 9)), metrics.linesOfCode);
    // TODO should be 1, 7, 8, 9
    assertEquals(0, metrics.commentLines.size());
  }

  private String getAndWriteActual(String ruleName, Map<String, List<String>> issuesPreFile) throws IOException {
    StringBuilder actual = new StringBuilder();
    List<Map.Entry<String, List<String>>> entries = new ArrayList<>(issuesPreFile.entrySet());
    entries.sort(Comparator.comparing(Map.Entry::getKey));
    for (Map.Entry<String, List<String>> issuesPreFileEntry : entries) {
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
    UastNode uast = getGoUast(path);
    Engine engine = new Engine();
    for (Issue issue : engine.scan(uast).issues) {
      String ruleName = issue.getRule().getClass().getSimpleName();
      String filename = GO_SOURCE_DIRECTORY.relativize(path).toString().replace('\\', '/');
      issuesPreRulePreFile
        .computeIfAbsent(ruleName, key -> new ConcurrentHashMap<>())
        .computeIfAbsent(filename, key -> new ArrayList<>())
        .add(" " + getNodeLocation(issue));
    }
  }

  private String getNodeLocation(Issue issue) {
    UastNode.Token token = issue.getNode().firstToken();
    if (token != null) {
      return token.line + ":" + token.column;
    }
    return "unknown";
  }

  private UastNode getGoUast(Path path) {
    try {
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
    } catch (IOException e) {
      throw new IllegalStateException(e.getClass().getSimpleName() + " for '" + path + "': " + e.getMessage(), e);
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
