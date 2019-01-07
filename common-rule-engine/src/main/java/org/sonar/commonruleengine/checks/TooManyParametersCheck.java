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

import java.util.ArrayList;
import java.util.List;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.uast.UastNode;
import org.sonar.uast.helpers.FunctionLike;

/**
 * https://jira.sonarsource.com/browse/RSPEC-107
 */
@Rule(key = "S107")
public class TooManyParametersCheck extends Check {

  private static final int DEFAULT_MAXIMUM = 7;

  @RuleProperty(
    key = "max",
    description = "Maximum authorized number of parameters",
    defaultValue = "" + DEFAULT_MAXIMUM)
  public int maximum = DEFAULT_MAXIMUM;

  public TooManyParametersCheck() {
    super(UastNode.Kind.FUNCTION);
  }

  @Override
  public void visitNode(UastNode node) {
    FunctionLike function = FunctionLike.from(node);
    if (function == null) {
      return;
    }
    List<UastNode> parameters = function.parameters();
    int parameterCount = 0;
    for (UastNode parameter : parameters) {
      List<UastNode> identifiers = new ArrayList<>();
      parameter.getDescendants(UastNode.Kind.IDENTIFIER, identifiers::add, UastNode.Kind.TYPE);
      parameterCount += identifiers.size();
    }
    if (parameterCount > maximum) {
      reportIssue(function.name(),
        String.format("Function has %d parameters, which is more than %d authorized.", parameterCount, maximum));
    }
  }
}
