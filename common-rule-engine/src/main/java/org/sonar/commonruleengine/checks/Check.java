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

import org.sonar.commonruleengine.Issue;
import org.sonar.uast.UastNode;
import org.sonar.uast.Visitor;

public abstract class Check extends Visitor {

  public Check(UastNode.Kind... nodeKindsToVisit) {
    super(nodeKindsToVisit);
  }

  protected final void reportIssue(UastNode node, String message) {
    context.reportIssue(new Issue(this, new Issue.Message(node, message), null));
  }

  protected final void reportIssue(UastNode node, String message, Issue.Message... secondaryMessages) {
    context.reportIssue(new Issue(this, new Issue.Message(node, message), null, secondaryMessages));
  }

  protected final void reportIssue(UastNode from, UastNode to, String message, Issue.Message... secondaryMessages) {
    context.reportIssue(new Issue(this, new Issue.Message(from, to, message), null, secondaryMessages));
  }

  protected final void reportIssue(UastNode from, UastNode to, String message, double effortToFix, Issue.Message... secondaryMessages) {
    context.reportIssue(new Issue(this, new Issue.Message(from, to, message), effortToFix, secondaryMessages));
  }

  protected final void reportIssueOnFile(String message) {
    context.reportIssue(Issue.issueOnFile(this, message));
  }

  protected final void reportIssueOnLine(int line, String message) {
    context.reportIssue(Issue.issueOnLine(this, line, message));
  }
}
