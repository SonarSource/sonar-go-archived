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

import java.util.List;
import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.commonruleengine.EngineContext;
import org.sonar.uast.UastNode;
import org.sonar.uast.UastNode.Kind;
import org.sonar.uast.UastNode.Token;

@Rule(key = "S100")
public class FunctionNameConventionCheck extends Check {

  private static final String MESSAGE = "Rename \'%s\' to match the regular expression %s.";
  private static final String DEFAULT_FORMAT = "^[a-zA-Z0-9]+$";

  @RuleProperty(
    key = "format",
    description = "Regular expression used to check the function names against",
    defaultValue = DEFAULT_FORMAT)
  public String format = DEFAULT_FORMAT;

  private Pattern namePattern;

  public FunctionNameConventionCheck() {
    super(Kind.FUNCTION);
  }

  @Override
  public void initialize(EngineContext context) {
    super.initialize(context);
    namePattern = Pattern.compile(format);
  }

  @Override
  public void visitNode(UastNode node) {
    if (node.is(Kind.CONSTRUCTOR)) {
      return;
    }

    List<UastNode> nameNodes = node.getChildren(Kind.FUNCTION_NAME);
    if (nameNodes.size() != 1) {
      return;
    }
    UastNode nameNode = nameNodes.get(0);
    Token firstToken = nameNode.firstToken();
    if (nameNode.is(Kind.IDENTIFIER) && firstToken != null) {
      String functionName = firstToken.value;
      if (!namePattern.matcher(functionName).matches()) {
        reportIssue(nameNode, String.format(MESSAGE, functionName, format));
      }
    }
  }

}
