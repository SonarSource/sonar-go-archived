/*
 * SonarQube Go Plugin
 * Copyright (C) 2018-2018 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
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
