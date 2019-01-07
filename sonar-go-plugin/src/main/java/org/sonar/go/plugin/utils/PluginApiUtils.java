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
package org.sonar.go.plugin.utils;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.uast.UastNode;

public class PluginApiUtils {
  private PluginApiUtils() {
    // utility class, forbidden constructor
  }

  public static TextRange newRange(InputFile inputFile, UastNode.Token token) {
    return inputFile.newRange(
      token.line,
      token.column - 1,
      token.endLine,
      token.endColumn);
  }

  public static TextRange newRange(InputFile inputFile, UastNode from, UastNode to) {
    if (from.is(UastNode.Kind.COMMENT, UastNode.Kind.STRUCTURED_COMMENT)) {
      return newRange(inputFile, from.token);
    }
    UastNode.Token firstToken = from.firstToken();
    UastNode.Token lastToken = to.lastToken();
    return inputFile.newRange(
      firstToken.line,
      firstToken.column - 1,
      lastToken.endLine,
      lastToken.endColumn);
  }

}
