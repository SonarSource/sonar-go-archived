/*
 * SonarQube Go Plugin
 * Copyright (C) 2018-2019 SonarSource SA
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
      String message = "Refactor this function to reduce its Cognitive Complexity from " +
        complexity.value() + " to the " + maxComplexity + " allowed.";
      int effortToFix = complexity.value() - maxComplexity;
      reportIssue(functionNode.name(), functionNode.name(), message, effortToFix, complexity.secondaryLocations());
    }
  }

  @Override
  public void leaveNode(UastNode node) {
    nestedFunctionLevel--;
  }

}
