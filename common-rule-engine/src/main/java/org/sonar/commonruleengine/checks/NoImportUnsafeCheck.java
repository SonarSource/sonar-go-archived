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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.commonruleengine.EngineContext;
import org.sonar.uast.UastNode;

@Rule(key = "X9999")
public class NoImportUnsafeCheck extends Check {

  private static final String DEFAULT_UNSAFE_PACKAGES = "unsafe";
  private static final String MESSAGE = "An import of the potentially insecure package 'unsafe' has been detected.";

  private Pattern targetPattern;

  public NoImportUnsafeCheck() {
    super(UastNode.Kind.IMPORT);
  }

  @Override
  public void initialize(EngineContext context) {
    super.initialize(context);
    String pattern = DEFAULT_UNSAFE_PACKAGES;
    targetPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
  }

  @Override
  public void visitNode(UastNode node) {
    if (node.is(UastNode.Kind.IMPORT)) {
      for (UastNode child : node.children) {
        testPattern(child, targetPattern);
      }
    }
  }

  private void testPattern(UastNode node, Pattern pattern) {
    String nodeValue = node.joinTokens();
    Matcher matcher = pattern.matcher(nodeValue);
    if (matcher.find()) {
      reportIssue(node, String.format(MESSAGE, matcher.group(0)));
    }
  }
}
