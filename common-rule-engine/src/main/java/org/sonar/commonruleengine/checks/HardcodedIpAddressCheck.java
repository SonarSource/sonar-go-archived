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

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.uast.UastNode;
import org.sonar.uast.helpers.LiteralLike;

@Rule(key = "S1313")
public class HardcodedIpAddressCheck extends Check {

  private static final Pattern IPV4_PATTERN = Pattern.compile("([^\\d.]*\\/)?(?<ip>(?:\\d{1,3}\\.){3}\\d{1,3}(?!\\d|\\.))(:(\\d{1,5}))?(\\/.*)?");

  public HardcodedIpAddressCheck() {
    super(UastNode.Kind.STRING_LITERAL);
  }

  @Override
  public void visitNode(UastNode node) {
    LiteralLike literal = LiteralLike.from(node);
    String content = removeQuotes(literal.value());
    Matcher ipv4Matcher = IPV4_PATTERN.matcher(content);
    if (ipv4Matcher.matches()) {
      String ip = ipv4Matcher.group("ip");
      if (isValidIpAddress(ip)) {
        reportIssue(literal.node(), "Make this IP \"" + ip + "\" address configurable.");
      }
    }
  }

  private static String removeQuotes(String string) {
    if (string.length() >= 2) {
      return string.substring(1, string.length() - 1);
    }
    return string;
  }

  private static boolean isValidIpAddress(String ip) {
    return Arrays.stream(ip.split("\\.")).mapToInt(Integer::parseInt)
      .noneMatch(num -> num > 255);
  }
}
