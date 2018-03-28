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

import java.io.IOException;
import java.util.Arrays;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.commonruleengine.EngineContext;
import org.sonar.commonruleengine.Issue;
import org.sonar.uast.UastNode;

public abstract class Check {

  private final UastNode.Kind[] kinds;
  protected EngineContext context;

  public Check(UastNode.Kind... nodeKindsToVisit) {
    this.kinds = nodeKindsToVisit;
  }

  /**
   * This method is called only once by analysis
   */
  public void initialize(EngineContext context) {
    this.context = context;
    Arrays.stream(kinds).forEach(kind -> context.register(kind, this));
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

  protected final void reportIssue(UastNode node, String message) {
    context.reportIssue(new Issue(this, new Issue.Message(node, message)));
  }

  protected final void reportIssue(UastNode node, String message, Issue.Message... secondaryMessages) {
    context.reportIssue(new Issue(this, new Issue.Message(node, message), secondaryMessages));
  }

  protected final void reportIssue(UastNode from, UastNode to, String message, Issue.Message... secondaryMessages) {
    context.reportIssue(new Issue(this, new Issue.Message(from, to, message), secondaryMessages));
  }

  protected final void reportIssueOnFile(String message) {
    context.reportIssue(Issue.issueOnFile(this, message));
  }
}
