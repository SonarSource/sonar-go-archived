package org.sonar.commonruleengine.checks;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.commonruleengine.CognitiveComplexity;
import org.sonar.uast.UastNode;
import org.sonar.uast.helpers.FunctionLike;

/**
 * https://jira.sonarsource.com/browse/RSPEC-3776
 */
@Rule(key = "S3776")
public class FunctionCognitiveComplexityCheck extends Check {

  private static final int DEFAULT_MAXIMUM_FUNCTION_COMPLEXITY_THRESHOLD = 15;

  private int nestedFunctionLevel;

  @RuleProperty(
    key = "maximumFunctionCognitiveComplexityThreshold",
    description = "The maximum authorized complexity.",
    defaultValue = "" + DEFAULT_MAXIMUM_FUNCTION_COMPLEXITY_THRESHOLD)
  private int maxComplexity = DEFAULT_MAXIMUM_FUNCTION_COMPLEXITY_THRESHOLD;

  public FunctionCognitiveComplexityCheck() {
    super(UastNode.Kind.FUNCTION);
  }

  @Override
  public void enterFile(InputFile inputFile) {
    nestedFunctionLevel = 0;
  }

  public void setMaxComplexity(int maxComplexity) {
    this.maxComplexity = maxComplexity;
  }

  @Override
  public void visitNode(UastNode node) {
    nestedFunctionLevel++;
    if (nestedFunctionLevel != 1) {
      return;
    }
    FunctionLike functionNode = FunctionLike.from(node);
    if (functionNode == null) {
      return;
    }
    CognitiveComplexity complexity = CognitiveComplexity.calculateFunctionComplexity(functionNode.node());
    if (complexity.value() > maxComplexity) {
      // TODO effortToFix = complexity.value() - maxComplexity
      reportIssue(functionNode.name(), "Refactor this function to reduce its Cognitive Complexity from " +
          complexity.value() + " to the " + maxComplexity + " allowed.",
        complexity.secondaryLocations());
    }
  }

  @Override
  public void leaveNode(UastNode node) {
    nestedFunctionLevel--;
  }

}
