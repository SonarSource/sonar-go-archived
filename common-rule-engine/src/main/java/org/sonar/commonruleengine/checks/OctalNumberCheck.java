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

import org.sonar.check.Rule;
import org.sonar.uast.UastNode;
import org.sonar.uast.UastNode.Kind;
import org.sonar.uast.UastNode.Token;

@Rule(key = "S1314")
public class OctalNumberCheck extends Check {

  public OctalNumberCheck() {
    super(Kind.OCTAL_LITERAL);
  }

  @Override
  public void visitNode(UastNode node) {
    if (!isFilePermissionFormat(node)) {
      reportIssue(node, "Use decimal rather than octal values.");
    }
  }

  private static boolean isFilePermissionFormat(UastNode node) {
    Token token = node.firstToken();
    if (token != null) {
      String numberText = token.value;
      if (numberText.length() == 4) {
        return true;
      }
    }

    return false;
  }

}
