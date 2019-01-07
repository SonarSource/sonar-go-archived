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
package org.sonar.uast;

import java.io.IOException;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.commonruleengine.EngineContext;

public abstract class Visitor {

  private final UastNode.Kind[] kinds;
  protected EngineContext context;

  public Visitor(UastNode.Kind... nodeKindsToVisit) {
    this.kinds = nodeKindsToVisit;
  }

  /**
   * This method is called only once by analysis
   */
  public void initialize(EngineContext context) {
    this.context = context;
    for (UastNode.Kind kind : kinds) {
      context.register(kind, this);
    }
  }

  /**
   * This method is called every time we enter a new file, allowing state cleaning for checks
   * @param inputFile
   */
  public void enterFile(InputFile inputFile) throws IOException {
  }

  public abstract void visitNode(UastNode node);

  /**
   * This method is called after "visitNode(node)" of the node itself and all its descendants
   */
  public void leaveNode(UastNode node) {
  }
}
