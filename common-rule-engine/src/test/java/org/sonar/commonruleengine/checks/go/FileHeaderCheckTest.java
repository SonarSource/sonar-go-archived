package org.sonar.commonruleengine.checks.go;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.sonar.commonruleengine.checks.FileHeaderCheck;

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

    Path testFile = testFile("go", check.getClass(), "FileHeaderCheck.go");
    checkNoIssue(check, testFile, goUast(testFile));
    checkRuleOnGo(check, "FileHeaderCheckYear.go");
    checkRuleOnGo(check, "FileHeaderCheckBeforeHeader.go");
  }

  @Test
  void test_regex() throws Exception {
    FileHeaderCheck check = new FileHeaderCheck();
    check.headerFormat = "/\\*\nCopyright 204. ACME\n\n\\*/";
    check.isRegularExpression = true;

    Path testFile = testFile("go", check.getClass(), "FileHeaderCheck.go");
    checkNoIssue(check, testFile, goUast(testFile));
    testFile = testFile("go", check.getClass(), "FileHeaderCheckYearNoIssue.go");
    checkNoIssue(check, testFile, goUast(testFile));
    checkRuleOnGo(check, "FileHeaderCheckBeforeHeader.go");
  }

}
