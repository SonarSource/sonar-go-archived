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
import org.sonar.api.batch.sensor.cpd.NewCpdTokens;
import org.sonar.uast.UastNode;

import static org.sonar.go.plugin.utils.PluginApiUtils.newRange;

public class CpdVisitor {

  private final InputFile inputFile;
  private final NewCpdTokens cpdTokens;

  CpdVisitor(SensorContext sensorContext, InputFile inputFile) {
    this.inputFile = inputFile;
    cpdTokens = sensorContext.newCpdTokens().onFile(inputFile);
  }

  public void scan(UastNode node) {
    if (node.is(UastNode.Kind.COMMENT, UastNode.Kind.EOF)) {
      return;
    }

    UastNode.Token token = node.token;
    if (token != null) {
      String text = token.value;
      if (node.is(UastNode.Kind.LITERAL)) {
        text = "LITERAL";
      }

      cpdTokens.addToken(newRange(inputFile, token), text);
    }

    for (UastNode child : node.children) {
      scan(child);
    }
  }

  public void save() {
    cpdTokens.save();
  }

}
