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

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.uast.UastNode;

@Rule(key = "S1134")
public class FixMeTagPresenceCheck extends Check {

  private static final Pattern FIXME_PATTERN = Pattern.compile("\\WFIXME(\\W|$)");

  public FixMeTagPresenceCheck() {
    super(UastNode.Kind.COMMENT, UastNode.Kind.STRUCTURED_COMMENT);
  }

  @Override
  public void visitNode(UastNode node) {
    if (node.token != null) {
      String comment = node.token.value.toUpperCase(Locale.ENGLISH);
      Matcher matcher = FIXME_PATTERN.matcher(comment);
      if (matcher.find()) {
        reportIssue(node, "Take the required action to fix the issue indicated by this \"FIXME\" comment.");
      }
    }
  }
}
