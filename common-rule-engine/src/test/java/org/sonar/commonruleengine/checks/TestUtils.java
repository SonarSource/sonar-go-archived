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
package org.sonar.commonruleengine.checks;

import com.sonarsource.checks.verifier.SingleFileVerifier;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.commonruleengine.Engine;
import org.sonar.commonruleengine.Issue;
import org.sonar.uast.Uast;
import org.sonar.uast.UastNode;
import org.sonar.uast.generator.java.Generator;

public class TestUtils {

  public static void checkRuleOnJava(Check check) throws IOException {
    checkRuleOnJava(check, check.getClass().getSimpleName() + ".java");
  }

  public static void checkRuleOnJava(Check check, String filename) throws IOException {
    Path sourceFilename = testFile("java", check.getClass(), filename);
    Generator generator = new Generator(new String(Files.readAllBytes(sourceFilename)));
    checkRule(check, sourceFilename, Uast.from(new StringReader(generator.json())));
  }

  public static void checkRuleOnGo(Check check) throws IOException {
    checkRuleOnGo(check, check.getClass().getSimpleName() + ".go");
  }

  public static void checkRuleOnGo(Check check, String filename) throws IOException {
    Path testFile = testFile("go", check.getClass(), filename);
    UastNode uast = goUast(testFile);
    checkRule(check, testFile, uast);
  }

  public static UastNode goUast(Path testFile) throws IOException {
    return Uast.from(Files.newBufferedReader(Paths.get(testFile + ".uast.json")));
  }

  public static void checkRule(Check check, Path testFile, UastNode uast) throws IOException {
    SingleFileVerifier verifier = analyze(check, testFile, uast);
    verifier.assertOneOrMoreIssues();
  }

  public static void checkNoIssue(Check check, Path testFile, UastNode uast) throws IOException {
    SingleFileVerifier verifier = analyze(check, testFile, uast);
    verifier.assertNoIssues();
  }

  private static SingleFileVerifier analyze(Check check, Path testFile, UastNode uast) throws IOException {
    TestInputFileBuilder inputFile = TestInputFileBuilder.create("test", testFile.getParent().toFile(), testFile.toFile());
    inputFile.setContents(new String(Files.readAllBytes(testFile), StandardCharsets.UTF_8));
    inputFile.setCharset(StandardCharsets.UTF_8);

    Engine engine = new Engine(Collections.singletonList(check));
    List<Issue> issues = engine.scan(uast, inputFile.build()).issues;

    SingleFileVerifier verifier = SingleFileVerifier.create(testFile, StandardCharsets.UTF_8);
    uast.getDescendants(UastNode.Kind.COMMENT, comment -> verifier.addComment(comment.token.line, comment.token.column, comment.token.value, 2, 0));
    issues.forEach(issue -> reportIssueTo(verifier, issue));
    return verifier;
  }

  private static void reportIssueTo(SingleFileVerifier verifier, Issue issue) {
    SingleFileVerifier.Issue newIssue;
    if (issue.hasLocation()) {
      Issue.Message primary = issue.getPrimary();
      UastNode.Token from = primary.from.firstToken();
      UastNode.Token to = primary.to.lastToken();
      newIssue = verifier.reportIssue(issue.getMessage())
        .onRange(from.line, from.column, to.endLine, to.endColumn);
    } else {
      newIssue = verifier.reportIssue(issue.getMessage()).onFile();
    }
    Arrays.stream(issue.getSecondaries()).forEach(secondary -> reportSecondaryTo(newIssue, secondary));
  }

  private static void reportSecondaryTo(SingleFileVerifier.Issue newIssue, Issue.Message secondary) {
    UastNode.Token from = secondary.from.firstToken();
    UastNode.Token to = secondary.to.lastToken();
    newIssue.addSecondary(from.line, from.column, to.endLine, to.endColumn, secondary.description);
  }

  public static Path testFile(String prefix, Class<? extends Check> check, String filename) {
    return testFile(Paths.get(prefix, check.getSimpleName(), filename));
  }

  public static Path testFile(Path file) {
    return Paths.get("src/test/files/checks/").resolve(file);
  }

}
