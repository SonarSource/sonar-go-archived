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

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

import static org.sonar.commonruleengine.checks.TestUtils.checkNoIssue;
import static org.sonar.commonruleengine.checks.TestUtils.checkRuleOnGo;
import static org.sonar.commonruleengine.checks.TestUtils.goUast;
import static org.sonar.commonruleengine.checks.TestUtils.testFile;

class FileHeaderCheckTest {

  // TODO test with Windows EOL

  @Test
  void test() throws Exception {
    FileHeaderCheck check = new FileHeaderCheck();
    check.headerFormat = "/*\nCopyright 2049 ACME\n\n*/";

    Path testFile = testFile(check.getClass(), "FileHeaderCheck.go");
    checkNoIssue(check, testFile, goUast(testFile));
    checkRuleOnGo(check, "FileHeaderCheckYear.go");
    checkRuleOnGo(check, "FileHeaderCheckBeforeHeader.go");
  }

  @Test
  void test_regex() throws Exception {
    FileHeaderCheck check = new FileHeaderCheck();
    check.headerFormat = "/\\*\nCopyright 204. ACME\n\n\\*/";
    check.isRegularExpression = true;

    Path testFile = testFile(check.getClass(), "FileHeaderCheck.go");
    checkNoIssue(check, testFile, goUast(testFile));
    testFile = testFile(check.getClass(), "FileHeaderCheckYearNoIssue.go");
    checkNoIssue(check, testFile, goUast(testFile));
    checkRuleOnGo(check, "FileHeaderCheckBeforeHeader.go");
  }

}
