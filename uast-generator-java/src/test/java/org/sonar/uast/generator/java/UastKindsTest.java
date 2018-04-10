package org.sonar.uast.generator.java;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

public class UastKindsTest {

  @ParameterizedTest
  @MethodSource("files")
  void test(Path path) throws Exception {
    String source = fileContent(path);
    Generator generator = new Generator(source);
    String expected = fileContent(Paths.get(path + ".uast.json"));
    assertThat(generator.json()).isEqualTo(expected);
  }

  @Test
  void should_use_all_uast_kinds() throws Exception {
    // ensure that we test all kinds that can be produced
    List<UastNode.Kind> usedKinds = files().map(UastKindsTest::fileContent)
      .map(source -> new Generator(source).uast())
      .flatMap(node -> collectKinds(node).stream())
      .collect(Collectors.toList());

    EnumSet<UastNode.Kind> usedKindsSet = EnumSet.copyOf(usedKinds);
    assertThat(usedKindsSet).hasSameElementsAs(EnumSet.allOf(UastNode.Kind.class));
  }

  @Test
  void generate() throws Exception {
    files().forEach(p -> {
      try {
        Generator.main(new String[] {p.toString()});
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
  }

  static List<UastNode.Kind> collectKinds(UastNode uastNode) {
    List<UastNode.Kind> kinds = new ArrayList<>();
    collectKinds0(uastNode, kinds);
    return kinds;
  }

  private static void collectKinds0(UastNode uastNode, List<UastNode.Kind> kinds) {
    kinds.addAll(uastNode.kinds);
    uastNode.children.forEach(c -> collectKinds0(c, kinds));
  }

  static String fileContent(Path path) {
    try {
      return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  static Stream<Path> files() throws IOException {
    return Files.walk(Paths.get("src/test/files/kinds")).filter(p -> Files.isRegularFile(p) && p.toString().endsWith(".java"));
  }

}
