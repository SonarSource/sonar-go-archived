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

import com.sonarsource.checks.verifier.SingleFileVerifier;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonar.commonruleengine.checks.TestUtils;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UastNodeTest {

  private static final Pattern NONCOMPLIANT_MESSAGE_REGEX = Pattern.compile("Noncompliant[^\\{]*\\{\\{([^\\}]+)\\}\\}");

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
    UastNode node = Uast.from(new StringReader("{ token: { line: 12, column: 34, value: 'foo' } }"));
    assertThat(node.joinTokens()).isEqualTo("foo");
  }

  @Test
  void tokenize2() throws Exception {
    UastNode uastNode = Uast.from(new InputStreamReader(UastNodeTest.class.getResourceAsStream("/reference.java.uast.json")));
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
    UastNode node = Uast.from(new StringReader("{ token: { line: 12, column: 34, value: 'foo' } }"));
    assertThat(node.firstToken()).isEqualTo(node.lastToken());

    node = Uast.from(new StringReader("{ children: [" +
      "{ token: { line: 12, column: 34, value: 'first' } }, " +
      "{ token: { line: 12, column: 34, value: 'last' } }" +
      "]}"));
    assertThat(node.firstToken().value).isEqualTo("first");
    assertThat(node.lastToken().value).isEqualTo("last");

    node = Uast.from(new StringReader("{ children: [" +
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
