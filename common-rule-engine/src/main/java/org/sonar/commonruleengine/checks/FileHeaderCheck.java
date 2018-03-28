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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.commonruleengine.EngineContext;
import org.sonar.uast.UastNode;

/**
 * https://jira.sonarsource.com/browse/RSPEC-1451
 */
@Rule(key = "S1451")
public class FileHeaderCheck extends Check {

  private static final String DEFAULT_HEADER_FORMAT = "";

  @RuleProperty(
    key = "headerFormat",
    description = "Expected copyright and license header",
    defaultValue = DEFAULT_HEADER_FORMAT,
    type = "TEXT")
  public String headerFormat = DEFAULT_HEADER_FORMAT;

  @RuleProperty(
    key = "isRegularExpression",
    description = "Whether the headerFormat is a regular expression",
    defaultValue = "false")
  public boolean isRegularExpression = false;

  private Pattern headerPattern;

  public FileHeaderCheck() {
    super(UastNode.Kind.COMPILATION_UNIT);
  }

  @Override
  public void initialize(EngineContext context) {
    super.initialize(context);
    headerPattern = Pattern.compile(getHeaderFormat(), Pattern.DOTALL);
  }

  @Override
  public void enterFile(InputFile inputFile) throws IOException {
    String contents = inputFile.contents();
    if (!isRegularExpression) {
      contents = normalizeEOL(contents);
    }
    Matcher matcher = headerPattern.matcher(contents);
    if (!matcher.lookingAt()) {
      reportIssueOnFile("Add or update the header of this file.");
    }
  }

  private String getHeaderFormat() {
    if (isRegularExpression) {
      return normalizeRegexEOL(headerFormat);
    } else {
      return Pattern.quote(normalizeEOL(headerFormat));
    }
  }

  private static String normalizeRegexEOL(String headerFormat) {
    // we replace newlines with \R , which will match any kind of EOL
    return headerFormat.replaceAll("\r?\n", "\\\\R");
  }

  private static String normalizeEOL(String contents) {
    return contents.replace("\r", "");
  }

  @Override
  public void visitNode(UastNode node) {
    // not used
  }
}
