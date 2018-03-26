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
    Optional<UastNode> blockOptional = node.children.stream().filter(n -> n.is(UastNode.Kind.BLOCK)).findFirst();
    if (blockOptional.isPresent()) {
      blockOptional.ifPresent(this::findUnconditionalJump);
    } else {
      node.children.stream()
        .filter(n -> n.is(UastNode.Kind.STATEMENT))
        .findFirst().filter(n -> n.is(JUMP_KINDS))
        .ifPresent(this::reportIssue);
    }
  }

  private void findUnconditionalJump(UastNode block) {
    for (UastNode child : block.children) {
      if (mayConditionallyContinue(child)) {
        return;
      }

      if (child.is(JUMP_KINDS)) {
        reportIssue(child);
        return;
      }

      // Only for Go. In Go, panic(...) is a CallExpr that is a child of an ExprStmt (= STATEMENT)
      if (child.is(UastNode.Kind.STATEMENT)) {
        Optional<UastNode> jumpOptional = child.children.stream().filter(c2 -> c2.is(JUMP_KINDS)).findFirst();
        if (jumpOptional.isPresent()) {
          reportIssue(jumpOptional.get());
          return;
        }
      }
    }
  }

  private static boolean mayConditionallyContinue(UastNode child) {
    if (!child.is(UastNode.Kind.IF)) {
      return false;
    }
    return child.children.stream()
      .filter(n -> n.is(UastNode.Kind.BLOCK, UastNode.Kind.STATEMENT))
      .findFirst()
      .flatMap(n -> n.children.stream().filter(n2 -> n2.is(UastNode.Kind.CONTINUE)).findFirst()).isPresent();
  }

  private void reportIssue(UastNode statement) {
    reportIssue(statement, "Remove this unconditional jump or make it conditional.");
  }
}
