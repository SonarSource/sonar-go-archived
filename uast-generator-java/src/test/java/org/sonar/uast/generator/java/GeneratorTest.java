package org.sonar.uast.generator.java;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.sonar.uast.UastNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

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
    assertEquals(Collections.singleton(UastNode.Kind.CLASS), classNode.kinds);
    assertEquals(5, classNode.children.size());

    UastNode eofToken = cutNode.children.get(1);
    assertEquals("TOKEN", eofToken.nativeNode);
    assertNotNull(eofToken.token);
    assertEquals(5, eofToken.token.line);
    assertEquals(2, eofToken.token.column);
    assertEquals("", eofToken.token.value);
    assertEquals(Collections.emptySet(), eofToken.kinds);
    assertEquals(0, eofToken.children.size());
  }

  @Test
  void fileContent() throws Exception {
    String source = new String(Files.readAllBytes(Paths.get("src/test/files/source.java")), StandardCharsets.UTF_8);
    String generatedUast = new Generator(source).json();
    String expectedUast = new String(Files.readAllBytes(Paths.get("src/test/files/source.java.uast.json")), StandardCharsets.UTF_8).trim();
    assertEquals(expectedUast, generatedUast);
  }

}
