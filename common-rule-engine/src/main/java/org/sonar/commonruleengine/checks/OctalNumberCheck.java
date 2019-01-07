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

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.check.Rule;
import org.sonar.uast.UastNode;
import org.sonar.uast.UastNode.Kind;
import org.sonar.uast.UastNode.Token;
import org.sonar.uast.helpers.AssignmentLike;
import org.sonar.uast.helpers.BinaryExpressionLike;

@Rule(key = "S1314")
public class OctalNumberCheck extends Check {

  private final Set<UastNode> bitwiseOperationArguments = new HashSet<>();

  public OctalNumberCheck() {
    super(
      Kind.OCTAL_LITERAL,
      Kind.BITWISE_AND,
      Kind.BITWISE_AND_NOT,
      Kind.BITWISE_OR,
      Kind.BITWISE_XOR,
      Kind.LEFT_SHIFT,
      Kind.RIGHT_SHIFT,
      Kind.LEFT_SHIFT_ASSIGNMENT,
      Kind.RIGHT_SHIFT_ASSIGNMENT,
      Kind.AND_ASSIGNMENT,
      Kind.AND_NOT_ASSIGNMENT,
      Kind.OR_ASSIGNMENT
    );
  }

  @Override
  public void enterFile(InputFile inputFile) throws IOException {
    bitwiseOperationArguments.clear();
  }

  @Override
  public void visitNode(UastNode node) {
    if (node.is(Kind.OCTAL_LITERAL)) {
      if (!isFilePermissionFormat(node) && !bitwiseOperationArguments.contains(node)) {
        reportIssue(node, "Use decimal rather than octal values.");
      }

    } else {
      BinaryExpressionLike binaryExpression = BinaryExpressionLike.from(node);
      if (binaryExpression != null) {
        bitwiseOperationArguments.add(binaryExpression.leftOperand());
        bitwiseOperationArguments.add(binaryExpression.rightOperand());
      } else {
        AssignmentLike assignmentLike = AssignmentLike.from(node);
        if (assignmentLike != null) {
          assignmentLike.value().getDescendants(Kind.ASSIGNMENT_VALUE, bitwiseOperationArguments::add);
        }
      }
    }
  }

  private static boolean isFilePermissionFormat(UastNode node) {
    Token token = node.firstToken();
    if (token != null) {
      String numberText = token.value;
      // 4-digit octal numbers are ignored as they are often used for file permissions
      if (numberText.length() == 4) {
        return true;
      }
    }

    return false;
  }

}
