package org.sonar.commonruleengine.checks;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.commonruleengine.EngineContext;
import org.sonar.uast.UastNode;
import org.sonar.uast.helpers.AssignmentLike;
import org.sonar.uast.helpers.LiteralLike;

/**
 * https://jira.sonarsource.com/browse/RSPEC-2068
 */
@Rule(key = "S2068")
public class NoHardcodedCredentialsCheck extends Check {

  private static final String DEFAULT_CREDENTIAL_WORDS = "password,passwd,pwd";
  private static final String MESSAGE = "'%s' detected in identifier; remove this potentially hardcoded credential.";

  @RuleProperty(
    key = "credentialWords",
    description = "Comma separated list of words identifying potential credentials",
    defaultValue = DEFAULT_CREDENTIAL_WORDS)
  public String credentialWords = DEFAULT_CREDENTIAL_WORDS;

  private Pattern targetPattern;
  private Pattern valuePattern;

  public NoHardcodedCredentialsCheck() {
    super(UastNode.Kind.ASSIGNMENT);
  }

  @Override
  public void initialize(EngineContext context) {
    super.initialize(context);
    String[] words = credentialWords.split(",");
    String pattern = Arrays.stream(words).map(Pattern::quote).collect(Collectors.joining("|"));
    targetPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
    pattern = Arrays.stream(words).map(Pattern::quote).map(w -> w + "=[^\\s\"]").collect(Collectors.joining("|"));
    valuePattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
  }

  @Override
  public void visitNode(UastNode node) {
    AssignmentLike assignment = AssignmentLike.from(node);
    if (assignment != null) {
      LiteralLike assignmentValue = LiteralLike.from(assignment.value());
      if (assignmentValue != null && !removeQuotes(assignmentValue.value()).isEmpty()) {
        testPattern(assignment.target(), targetPattern);
        testPattern(assignment.value(), valuePattern);
      }
    }
  }

  private static String removeQuotes(String string) {
    if (string.length() >= 2) {
      return string.substring(1, string.length() - 1);
    }
    return string;
  }

  private void testPattern(UastNode node, Pattern pattern) {
    String nodeValue = node.joinTokens();
    Matcher matcher = pattern.matcher(nodeValue);
    if (matcher.find()) {
      reportIssue(node, String.format(MESSAGE, matcher.group(0)));
    }
  }
}
