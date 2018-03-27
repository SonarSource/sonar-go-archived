package org.sonar.commonruleengine.checks;

import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.uast.UastNode;

/**
 * https://jira.sonarsource.com/browse/RSPEC-1751
 */
@Rule(key = "S1751")
public class UnconditionalJumpStatementCheck extends Check {

  private static final UastNode.Kind[] JUMP_KINDS = {UastNode.Kind.BREAK, UastNode.Kind.RETURN, UastNode.Kind.THROW, UastNode.Kind.CONTINUE};

  public UnconditionalJumpStatementCheck() {
    super(UastNode.Kind.LOOP);
  }

  @Override
  public void visitNode(UastNode node) {
    if (node.is(UastNode.Kind.LOOP_FOREACH)) {
      return;
    }

    Optional<UastNode> blockOptional = node.children.stream().filter(UastNode.Kind.BLOCK).findFirst();
    if (blockOptional.isPresent()) {
      findUnconditionalJump(blockOptional.get());
    } else {
      node.children.stream()
        .filter(UastNode.Kind.STATEMENT)
        .findFirst().filter(n -> n.is(JUMP_KINDS))
        .ifPresent(this::reportIssue);
    }
  }

  private void findUnconditionalJump(UastNode block) {
    for (UastNode statement : block.children) {
      if (statement.is(UastNode.Kind.CONTINUE)) {
        reportIssue(statement);
        return;
      }

      if (statement.hasDescendant(UastNode.Kind.CONTINUE)) {
        return;
      }

      if (statement.is(JUMP_KINDS)) {
        reportIssue(statement);
        return;
      }
    }
  }

  private void reportIssue(UastNode statement) {
    reportIssue(statement, "Remove this unconditional jump or make it conditional.");
  }
}
