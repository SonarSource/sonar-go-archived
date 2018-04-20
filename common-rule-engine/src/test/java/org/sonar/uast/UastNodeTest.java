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
package org.sonar.uast;

import com.google.gson.JsonParseException;
import com.sonarsource.checks.verifier.SingleFileVerifier;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.commonruleengine.checks.TestUtils;
import org.sonar.uast.generator.java.Generator;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.sonar.commonruleengine.checks.TestUtils.createGoParserKinds;
import static org.sonar.commonruleengine.checks.TestUtils.uast;

class UastNodeTest {

  private static final Pattern NONCOMPLIANT_MESSAGE_REGEX = Pattern.compile("Noncompliant[^\\{]*\\{\\{([^\\}]+)\\}\\}");

  @Test
  void kind_inheritance_related_to_CONDITIONAL_JUMP() {
    List<UastNode.Kind> controlKinds = Arrays.asList(UastNode.Kind.IF, UastNode.Kind.SWITCH, UastNode.Kind.FOR);

    controlKinds.forEach(kind -> assertThat(kind.extendedKinds()).describedAs(kind.name())
      .contains(UastNode.Kind.CONDITIONAL_JUMP));

    controlKinds.forEach(kind -> assertThat(uast("{ kinds: ['" + kind.name() + "'] }").kinds).describedAs(kind.name())
      .contains(kind, UastNode.Kind.CONDITIONAL_JUMP));
  }

  @Test
  void kind_inheritance_related_to_UNCONDITIONAL_JUMP() {
    List<UastNode.Kind> controlKinds = Arrays.asList(UastNode.Kind.RETURN, UastNode.Kind.GOTO, UastNode.Kind.BREAK,
      UastNode.Kind.CONTINUE, UastNode.Kind.THROW);

    controlKinds.forEach(kind -> assertThat(kind.extendedKinds()).describedAs(kind.name())
      .containsExactlyInAnyOrder(UastNode.Kind.UNCONDITIONAL_JUMP));

    controlKinds.forEach(kind -> assertThat(uast("{ kinds: ['" + kind.name() + "'] }").kinds).describedAs(kind.name())
      .containsExactlyInAnyOrder(kind, UastNode.Kind.UNCONDITIONAL_JUMP));
  }

  @Test
  void invalid_token_position() {
    assertEquals("Invalid token location 0:0",
      assertThrows(IllegalArgumentException.class,
        () -> new UastNode.Token(0, 0, "token"))
          .getMessage());
  }

  @Test
  void token_position() {
    UastNode.Token token = new UastNode.Token(2, 4, "if");
    assertEquals(2, token.line);
    assertEquals(4, token.column);
    assertEquals(2, token.endLine);
    assertEquals(5, token.endColumn);

    token = new UastNode.Token(10, 1, "/* start \n end */");
    assertEquals(10, token.line);
    assertEquals(1, token.column);
    assertEquals(11, token.endLine);
    assertEquals(7, token.endColumn);
  }

  @Test
  void tokenize() throws Exception {
    UastNode node = UastNode.from(new StringReader("{ token: { line: 12, column: 34, value: 'foo' } }"));
    assertThat(node.joinTokens()).isEqualTo("foo");
  }

  @Test
  void tokenize2() throws Exception {
    UastNode uastNode = UastNode.from(new InputStreamReader(UastNodeTest.class.getResourceAsStream("/reference.java.uast.json")));
    assertThat(uastNode.joinTokens()).isEqualTo("class A {\n" +
      "  void foo() {\n" +
      "    System.out.println(\"yolo\");\n" +
      "  }\n" +
      "}\n");
  }

  @Test
  void test_utf32_tokens() {
    UastNode.Token token = new UastNode.Token(1, 1, "𝜶");
    assertThat(token.endColumn).isEqualTo(token.column);
  }

  @Test
  void test_first_last_token() throws Exception {
    UastNode node = UastNode.from(new StringReader("{ token: { line: 12, column: 34, value: 'foo' } }"));
    assertThat(node.firstToken()).isEqualTo(node.lastToken());

    node = UastNode.from(new StringReader("{ children: [" +
      "{ token: { line: 12, column: 34, value: 'first' } }, " +
      "{ token: { line: 12, column: 34, value: 'last' } }" +
      "]}"));
    assertThat(node.firstToken().value).isEqualTo("first");
    assertThat(node.lastToken().value).isEqualTo("last");

    node = UastNode.from(new StringReader("{ children: [" +
      "{ kinds: [] }, " +
      "{ token: { line: 12, column: 34, value: 'first' } }," +
      "{ token: { line: 12, column: 34, value: 'last' } }," +
      "{ kinds: [] }" +
      "]}"));

    assertThat(node.firstToken().value).isEqualTo("first");
    assertThat(node.lastToken().value).isEqualTo("last");
  }

  static List<Path> testKindSources(String fileExtension) throws IOException {
    Path basDirectory = Paths.get("src", "test", "files", "kinds");
    return Files.list(basDirectory)
      .filter(path -> path.toString().endsWith(fileExtension))
      .collect(Collectors.toList());
  }

  static List<Path> goTestKindSources() throws IOException {
    return testKindSources(".go");
  }

  @Test
  void go_parser_kinds_are_subset_of_common_kinds() throws IOException {
    String[] lines = new String(Files.readAllBytes(createGoParserKinds()), UTF_8).split("\r?\n");
    for (String line : lines) {
      if (!line.isEmpty()) {
        UastNode.Kind.valueOf(line);
      }
    }
  }

  @Test
  void java_parser_kinds_are_subset_of_common_kinds() {
    for (String name : Generator.allKindNames()) {
      UastNode.Kind.valueOf(name);
    }
  }


  @Test
  void parse_kind() throws Exception {
    UastNode node = UastNode.from(new StringReader("{ kinds: [ 'IDENTIFIER', 'UNKNOWN1', 'IDENTIFIER', 'UNKNOWN2' ] }"));
    assertEquals(EnumSet.of(UastNode.Kind.IDENTIFIER), node.kinds);
  }

  @Test
  void parse_native_node() throws Exception  {
    UastNode node = UastNode.from(new StringReader("{ nativeNode: 'foo' }"));
    assertEquals("foo", node.nativeNode);
  }

  @Test
  void parse_token() throws Exception  {
    UastNode node = UastNode.from(new StringReader("{ token: { line: 1, column: 1, value: null } }"));
    assertEquals(Collections.emptyList(), node.children);
    assertNotNull(node.token);
    assertEquals(1, node.token.line);
    assertEquals(1, node.token.column);
    assertEquals("", node.token.value);

    node = UastNode.from(new StringReader("{ token: { line: 12, column: 34, value: 'foo' } }"));
    assertNotNull(node.token);
    assertEquals(12, node.token.line);
    assertEquals(34, node.token.column);
    assertEquals("foo", node.token.value);

    node = UastNode.from(new StringReader("{ kinds: [ 'EOF' ], token: { line: 345, column: 1 } }"));
    assertEquals(EnumSet.of(UastNode.Kind.EOF), node.kinds);
    assertNotNull(node.token);
    assertEquals(345, node.token.line);
    assertEquals(1, node.token.column);
    assertEquals("", node.token.value);
  }

  @Test
  void parse_invalid_token() {
    assertEquals("Attributes 'line' and 'column' are mandatory on 'token' object.",
      assertThrows(JsonParseException.class,
        () ->  UastNode.from(new StringReader("{ token: { } }")))
        .getMessage());
  }

  @Test
  void parse_children() throws Exception  {
    UastNode node = UastNode.from(new StringReader("{ children: [ { kinds: [ 'EOF' ] } ] }"));
    assertEquals(1, node.children.size());
    assertEquals(EnumSet.of(UastNode.Kind.EOF), node.children.get(0).kinds);
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "{}",
    "{ kinds: [], nativeNode: null, token: null, children: null, unknownElement: {} }",
    "{ kinds: [], nativeNode: '', token: null, children: [] }"
  })
  void parse_null_and_empty(String json) throws Exception  {
    Supplier<String> message = () -> "Assertion error for " + json;
    UastNode node = UastNode.from(new StringReader(json));
    assertEquals(Collections.emptySet(), node.kinds, message);
    assertEquals("", node.nativeNode, message);
    assertNull(node.token, message);
    assertEquals(Collections.emptyList(), node.children, message);
  }

  @ParameterizedTest
  @MethodSource("goTestKindSources")
  void test_go_kind(Path sourcePath) throws IOException {
    UastNode uast = TestUtils.goUast(sourcePath);
    SingleFileVerifier verifier = SingleFileVerifier.create(sourcePath, UTF_8);
    uast.getDescendants(UastNode.Kind.COMMENT, comment -> verifier.addComment(comment.token.line, comment.token.column, comment.token.value, 2, 0));
    Set<UastNode.Kind> testedKinds = getTestedKinds(sourcePath);
    descendantsWithKinds(uast, testedKinds).forEach(node -> {
      UastNode.Token from = node.firstToken();
      UastNode.Token to = node.lastToken();
      String message = testedKinds.stream()
        .filter(node::is)
        .map(UastNode.Kind::name)
        .collect(Collectors.joining(","));
      verifier.reportIssue(message).onRange(from.line, from.column, to.endLine, to.endColumn);
    });
    verifier.assertOneOrMoreIssues();
  }

  static Set<UastNode> descendantsWithKinds(UastNode parent, Set<UastNode.Kind> kinds) {
    Set<UastNode> children = new LinkedHashSet<>();
    kinds.forEach(kind -> parent.getDescendants(kind, children::add));
    return children;
  }

  static Set<UastNode.Kind> getTestedKinds(Path sourcePath) throws IOException {
    String source = new String(Files.readAllBytes(sourcePath), UTF_8);
    Matcher matcher = NONCOMPLIANT_MESSAGE_REGEX.matcher(source);
    Set<UastNode.Kind> kinds = new LinkedHashSet<>();
    while (matcher.find()) {
      Arrays.stream(matcher.group(1).split(",")).forEach(name -> kinds.add(UastNode.Kind.valueOf(name)));
    }
    if (kinds.isEmpty()) {
      throw new IllegalStateException("The file should contains at least one kind: " + sourcePath);
    }
    return kinds;
  }
}
