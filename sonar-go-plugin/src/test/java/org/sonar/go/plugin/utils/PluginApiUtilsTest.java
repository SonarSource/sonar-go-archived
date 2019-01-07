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

import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.uast.UastNode;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PluginApiUtilsTest {

  @Test
  void newRange_should_call_newRange_on_inputFile() {
    String value = "dummy value";
    int line = 7;
    int column = 13;

    UastNode.Token token = new UastNode.Token(line, column, value);
    InputFile inputFile = mock(InputFile.class);
    PluginApiUtils.newRange(inputFile, token);

    verify(inputFile).newRange(eq(line), eq(column - 1), eq(line), eq(column - 1 + value.length()));
  }

  @Test
  void newRange_should_use_single_token_for_comments() {
    String value = "// this is\nmy comment!";
    int line = 7;
    int column = 13;
    int endColumn = 11;

    UastNode.Token commentToken = new UastNode.Token(line, column, value);
    UastNode commentNode = new UastNode(Collections.singleton(UastNode.Kind.COMMENT), null, commentToken, Collections.emptyList());
    InputFile inputFile = mock(InputFile.class);
    PluginApiUtils.newRange(inputFile, commentNode, commentNode);

    verify(inputFile).newRange(eq(line), eq(column - 1), eq(line + 1), eq(endColumn));
  }

  @Test
  void newRange_should_use_single_token_for_structuredcomments() {
    String value = "/* this is\nmy comment! */";
    int line = 7;
    int column = 13;
    int endColumn = 14;

    UastNode.Token commentToken = new UastNode.Token(line, column, value);
    UastNode commentNode = new UastNode(Collections.singleton(UastNode.Kind.STRUCTURED_COMMENT), null, commentToken, Collections.emptyList());
    InputFile inputFile = mock(InputFile.class);
    PluginApiUtils.newRange(inputFile, commentNode, commentNode);

    verify(inputFile).newRange(eq(line), eq(column - 1), eq(line + 1), eq(endColumn));
  }
}
