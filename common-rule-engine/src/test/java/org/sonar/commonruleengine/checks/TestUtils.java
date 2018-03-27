package org.sonar.commonruleengine.checks;

import com.sonarsource.checks.verifier.SingleFileVerifier;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.commonruleengine.Engine;
import org.sonar.commonruleengine.Issue;
import org.sonar.uast.Uast;
import org.sonar.uast.UastNode;
import org.sonar.uast.generator.java.Generator;

public class TestUtils {

  public static void checkRuleOnJava(Check check) throws IOException {
    String checkName = check.getClass().getSimpleName();
    checkRuleOnJava(check, checkName + ".java");
  }

  public static void checkRuleOnJava(Check check, String filename) throws IOException {
    String sourceFilename = "java/" + check.getClass().getSimpleName() + "/" + filename;
    Generator generator = new Generator(new String(Files.readAllBytes(testFile(sourceFilename))));
    checkRule(check, testFile(sourceFilename), Uast.from(new StringReader(generator.json())));
  }

  public static void checkRuleOnGo(Check check) throws IOException {
    checkRuleOnGo(check, check.getClass().getSimpleName() + ".go");
  }

  public static void checkRuleOnGo(Check check, String filename) throws IOException {
    String sourceFilename = "go/" + check.getClass().getSimpleName() + "/" + filename;
    Path testFile = testFile(sourceFilename);
    UastNode uast = Uast.from(Files.newBufferedReader(Paths.get(testFile + ".uast.json")));
    checkRule(check, testFile, uast);
  }

  public static void checkRule(Check check, Path testFile, UastNode uast) {
    Engine engine = new Engine(Collections.singletonList(check));
    List<Issue> issues = engine.scan(uast, InputFile.Type.MAIN).issues;

    SingleFileVerifier verifier = SingleFileVerifier.create(testFile, StandardCharsets.UTF_8);
    uast.getDescendants(UastNode.Kind.COMMENT, comment -> verifier.addComment(comment.token.line, comment.token.column, comment.token.value, 2, 0));
    issues.forEach(issue -> reportIssueTo(verifier, issue));
    verifier.assertOneOrMoreIssues();
  }

  private static void reportIssueTo(SingleFileVerifier verifier, Issue issue) {
    Issue.Message primary = issue.getPrimary();
    UastNode.Token from = primary.from.firstToken();
    UastNode.Token to = primary.to.lastToken();
    SingleFileVerifier.Issue newIssue = verifier.reportIssue(primary.description)
      .onRange(from.line, from.column, to.endLine, to.endColumn);
    Arrays.stream(issue.getSecondaries()).forEach(secondary -> reportSecondaryTo(newIssue, secondary));
  }

  private static void reportSecondaryTo(SingleFileVerifier.Issue newIssue, Issue.Message secondary) {
    UastNode.Token from = secondary.from.firstToken();
    UastNode.Token to = secondary.to.lastToken();
    newIssue.addSecondary(from.line, from.column, to.endLine, to.endColumn, secondary.description);
  }

  private static Path testFile(String filename) {
    return Paths.get("src/test/files/checks/" + filename);
  }

}
