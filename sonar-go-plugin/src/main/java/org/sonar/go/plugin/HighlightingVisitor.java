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
package org.sonar.go.plugin;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.highlighting.NewHighlighting;
import org.sonar.api.batch.sensor.highlighting.TypeOfText;
import org.sonar.uast.UastNode;
import org.sonar.uast.UastNode.Kind;

import static org.sonar.go.plugin.utils.PluginApiUtils.newRange;

public class HighlightingVisitor {

  private final InputFile inputFile;
  private final NewHighlighting newHighlighting;

  public HighlightingVisitor(SensorContext sensorContext, InputFile inputFile) {
    this.inputFile = inputFile;
    newHighlighting = sensorContext.newHighlighting()
      .onFile(inputFile);
  }

  public void scan(UastNode node) {
    scanRecursively(node, false);
  }

  public void scanRecursively(UastNode node, boolean parentIsATypeDefinition) {
    UastNode.Token token = node.token;
    boolean isATypeDefinition = parentIsATypeDefinition || node.is(Kind.TYPE);
    if (token != null) {
      if (node.is(Kind.COMMENT)) {
        highlight(token, node.is(Kind.STRUCTURED_COMMENT) ? TypeOfText.STRUCTURED_COMMENT : TypeOfText.COMMENT);
      } else if (node.is(Kind.KEYWORD)) {
        highlight(token, TypeOfText.KEYWORD);
      } else if (node.is(Kind.LITERAL)) {
        highlight(token, node.is(Kind.STRING_LITERAL) ? TypeOfText.STRING : TypeOfText.CONSTANT);
      } else if (isATypeDefinition) {
        highlight(token, TypeOfText.KEYWORD_LIGHT);
      }
    }
    for (UastNode child : node.children) {
      scanRecursively(child, isATypeDefinition);
    }
  }

  private NewHighlighting highlight(UastNode.Token token, TypeOfText typeOfText) {
    return newHighlighting.highlight(newRange(inputFile, token), typeOfText);
  }

  public void save() {
    newHighlighting.save();
  }

}
