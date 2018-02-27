package org.sonar.uast.generator.java;

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
    assertEquals(8, classNode.children.size());

    UastNode eofToken = cutNode.children.get(1);
    assertEquals("TOKEN", eofToken.nativeNode);
    assertNotNull(eofToken.token);
    assertEquals(5, eofToken.token.line);
    assertEquals(1, eofToken.token.column);
    assertEquals("", eofToken.token.value);
    assertEquals(Collections.emptySet(), eofToken.kinds);
    assertEquals(0, eofToken.children.size());
  }

  @Test
  void fileContent() throws Exception {
    String source = Generator.fileContent("src/test/files/source.java");
    String expectedUast = Generator.fileContent("src/test/files/source.java.uast.json");
    String generatedUast = new Generator(source).json();
    assertEquals(expectedUast, generatedUast);
  }

}
