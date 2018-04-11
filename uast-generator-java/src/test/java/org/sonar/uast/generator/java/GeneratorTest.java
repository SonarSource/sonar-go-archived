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

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.EnumSet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.sonar.uast.generator.java.UastNode.Kind.CLASS;
import static org.sonar.uast.generator.java.UastNode.Kind.EOF;
import static org.sonar.uast.generator.java.UastNode.Kind.STATEMENT;
import static org.sonar.uast.generator.java.UastNode.Kind.TYPE;

public class GeneratorTest {

  @Test
  void generate() {
    String source = "class A {\n"
      + "  void foo() {\n"
      + "    System.out.println(\"yolo\");\n"
      + "  }\n"
      + "}";
    Generator generator = new Generator(source);
    UastNode cutNode = generator.uast();

    assertEquals("COMPILATION_UNIT", cutNode.nativeNode);
    assertEquals(null, cutNode.token);
    assertEquals(Collections.singleton(UastNode.Kind.COMPILATION_UNIT), cutNode.kinds);
    assertEquals(2, cutNode.children.size());

    UastNode classNode = cutNode.children.get(0);
    assertEquals("CLASS", classNode.nativeNode);
    assertNull(classNode.token);
    assertEquals(EnumSet.of(CLASS, STATEMENT, TYPE), classNode.kinds);
    assertEquals(5, classNode.children.size());

    UastNode eofToken = cutNode.children.get(1);
    assertEquals("TOKEN", eofToken.nativeNode);
    assertNotNull(eofToken.token);
    assertEquals(5, eofToken.token.line);
    assertEquals(2, eofToken.token.column);
    assertEquals("", eofToken.token.value);
    assertEquals(Collections.singleton(EOF), eofToken.kinds);
    assertEquals(0, eofToken.children.size());
  }

  @Test
  void test_generator_main() throws Exception {
    Generator.main(new String[] {"src/test/files/source.java"});
    Path generatedFile = Paths.get("src/test/files/source.java.uast.json");
    String generatedUast = new String(Files.readAllBytes(generatedFile), StandardCharsets.UTF_8);
    String expectedUast = new String(Files.readAllBytes(Paths.get("src/test/files/reference.java.uast.json")), StandardCharsets.UTF_8).trim();
    assertEquals(expectedUast, generatedUast);
    Files.deleteIfExists(generatedFile);
  }

  @Test
  void test_generator_main_with_directory() throws Exception {
    Generator.main(new String[] {"src/test/files"});
    Path generatedFile = Paths.get("src/test/files/source.java.uast.json");
    String generatedUast = new String(Files.readAllBytes(generatedFile), StandardCharsets.UTF_8);
    String expectedUast = new String(Files.readAllBytes(Paths.get("src/test/files/reference.java.uast.json")), StandardCharsets.UTF_8).trim();
    assertEquals(expectedUast, generatedUast);
    Files.deleteIfExists(generatedFile);
  }

}
