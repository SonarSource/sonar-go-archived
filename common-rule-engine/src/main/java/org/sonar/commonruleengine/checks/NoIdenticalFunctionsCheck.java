package org.sonar.commonruleengine.checks;

import java.util.ArrayList;
import java.util.List;
import org.sonar.uast.UastNode;
import org.sonar.uast.helpers.FunctionLike;

import static org.sonar.uast.Uast.syntacticallyEquivalent;

/**
 * Rule https://jira.sonarsource.com/browse/RSPEC-4144
 */
public class NoIdenticalFunctionsCheck extends Check {

  private List<FunctionLike> functions = new ArrayList<>();

  public NoIdenticalFunctionsCheck() {
    super(UastNode.Kind.FUNCTION, UastNode.Kind.COMPILATION_UNIT, UastNode.Kind.CLASS);
  }

  @Override
  public void visitNode(UastNode node) {
    if (node.kinds.contains(UastNode.Kind.COMPILATION_UNIT) || node.kinds.contains(UastNode.Kind.CLASS)) {
      functions.clear();
    }
    if (node.kinds.contains(FunctionLike.KIND)) {
      FunctionLike thisFunction = FunctionLike.from(node);
      if (thisFunction == null) {
        return;
      }
      if (thisFunction.body().getChildren(UastNode.Kind.STATEMENT).size() < 2) {
        return;
      }
      for (FunctionLike function : functions) {
        if (syntacticallyEquivalent(thisFunction.body(), function.body()) && syntacticallyEquivalent(thisFunction.parameters(), function.parameters())) {
          reportIssue(node, "Function is identical with function on line " + function.node().firstToken().token.line);
          break;
        }
      }
      functions.add(thisFunction);
    }
  }
}
