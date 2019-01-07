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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.sonar.check.Rule;
import org.sonar.commonruleengine.Issue;
import org.sonar.commonruleengine.Issue.Message;
import org.sonar.uast.UastNode;
import org.sonar.uast.UastNode.Kind;

@Rule(key = "S1172")
public class UnusedParameterCheck extends Check {

  private static final String MESSAGE = "Remove the unused function parameter(s) \"%s\".";

  public UnusedParameterCheck() {
    super(Kind.FUNCTION, Kind.FUNCTION_LITERAL);
  }

  @Override
  public void visitNode(UastNode node) {
    Optional<UastNode> functionBody = node.getChild(Kind.BLOCK);
    if (isMethod(node) || !functionBody.isPresent()) {
      return;
    }

    List<UastNode> parameterNames = getParameterNameNodes(node);
    Set<String> identifiersInBody = getIdentifierNames(functionBody.get());

    List<UastNode> unusedParameters = parameterNames.stream()
      .filter(parameterName -> !isParameterUsed(parameterName, identifiersInBody))
      .collect(Collectors.toList());

    if (!unusedParameters.isEmpty()) {
      String parametersJoined = unusedParameters.stream()
        .map(parameter -> parameter.token.value)
        .collect(Collectors.joining("\", \""));
      String message = String.format(MESSAGE, parametersJoined);
      reportIssue(unusedParameters.get(0), message, secondaryMessages(unusedParameters));
    }
  }

  private static Set<String> getIdentifierNames(UastNode functionBody) {
    Set<String> identifiersInBody = new HashSet<>();
    functionBody.getDescendants(Kind.IDENTIFIER, identifier -> {
      if (identifier.token != null) {
        identifiersInBody.add(identifier.token.value);
      }
    });
    return identifiersInBody;
  }

  private static List<UastNode> getParameterNameNodes(UastNode functionNode) {
    List<UastNode> parameterNames = new ArrayList<>();
    functionNode.getDescendants(Kind.PARAMETER_LIST, parameterDeclaration ->
      parameterDeclaration.getDescendants(Kind.VARIABLE_NAME, parameterName -> {
        // looks like parameter name is always identifier with token, this check is here for extra safety
        if (parameterName.is(Kind.IDENTIFIER) && parameterName.token != null) {
          parameterNames.add(parameterName);
        }
      })
    );
    return parameterNames;
  }

  private static boolean isParameterUsed(UastNode parameterName, Set<String> identifiersInBody) {
    String name = parameterName.token.value;
    return name.equals("_") || identifiersInBody.contains(name);
  }

  private static boolean isMethod(UastNode functionNode) {
    List<UastNode> parameterDeclarations = new ArrayList<>();
    functionNode.getDescendants(Kind.PARAMETER_LIST, parameterDeclarations::add);

    for (UastNode parameterDeclaration : parameterDeclarations) {
      if (parameterDeclaration.nativeNode.contains("Recv")) {
        return true;
      }
    }

    return false;
  }

  private static Issue.Message[] secondaryMessages(List<UastNode> nodes) {
    return nodes.stream().skip(1).map(Message::new).toArray(Issue.Message[]::new);
  }
}
