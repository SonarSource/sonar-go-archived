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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.utils.log.LoggerLevel;
import org.sonar.go.plugin.GoTestSensor.TestInfo;

import static org.assertj.core.api.Assertions.assertThat;

class GoTestSensorTest {

  @RegisterExtension
  static JUnit5LogTester logTester = new JUnit5LogTester();

  private final Path goPath = Paths.get("src", "test", "resources", "testReportGoPath").toAbsolutePath();
  private final Path packagePath = Paths.get("github.com", "myOrg", "myProject");

  @Test
  void absolute_package_path_in_report() throws IOException {
    Path packageAbsPath = goPath.resolve("src").resolve(packagePath);

    GoTestSensor goTestSensor = new GoTestSensor();
    goTestSensor.goPathContext = new GoPathContext(File.separatorChar, File.pathSeparator, null);
    String transformedPackageAbsPath;
    if (File.pathSeparator.equals(":")) {
      transformedPackageAbsPath = "_" + packageAbsPath;
    } else {
      transformedPackageAbsPath = "_\\" + packageAbsPath.toString().replaceFirst(":", "_");
    }
    TestInfo testInfo = new TestInfo("pass", transformedPackageAbsPath, "TestFoo", 42.);

    SensorContextTester contextTester = SensorContextTester.create(packageAbsPath);
    DefaultFileSystem fs = contextTester.fileSystem();
    DefaultInputFile testFile = getTestInputFile(fs, "func TestFoo(", "foo_test.go");

    InputFile foundTestFile = goTestSensor.findTestFile(fs, testInfo);
    assertThat(foundTestFile).isEqualTo(testFile);
  }

  @Test
  void relative_package_path_in_report() throws IOException {
    GoTestSensor goTestSensor = new GoTestSensor();
    goTestSensor.goPathContext = new GoPathContext(File.separatorChar, File.pathSeparator, goPath.toString());

    TestInfo testInfo = new TestInfo("pass", packagePath.toString(), "TestFoo", 42.);

    Path baseDir = goPath.resolve("src").resolve(packagePath);
    SensorContextTester contextTester = SensorContextTester.create(baseDir);
    DefaultFileSystem fs = contextTester.fileSystem();
    DefaultInputFile testFile = getTestInputFile(fs, "func TestFoo(", "foo_test.go");

    InputFile foundTestFile = goTestSensor.findTestFile(fs, testInfo);
    assertThat(foundTestFile).isEqualTo(testFile);
  }

  @Test
  void invalid_package_path_in_report() throws IOException {
    Path nestedPackagePath = packagePath.resolve("packageFoo");

    GoTestSensor goTestSensor = new GoTestSensor();
    goTestSensor.goPathContext = new GoPathContext(File.separatorChar, File.pathSeparator, null);

    TestInfo testInfoTop = new TestInfo("pass", packagePath.toString(), "TestFoo", 42.);
    TestInfo testInfoNested = new TestInfo("pass", nestedPackagePath.toString(), "TestFoo", 42.);

    Path baseDir = Paths.get("src", "test", "resources", "myProject").toAbsolutePath();
    SensorContextTester contextTester = SensorContextTester.create(baseDir);

    DefaultFileSystem fs = contextTester.fileSystem();

    DefaultInputFile topTestFile = getTestInputFile(fs, "func TestFoo(", "foo_test.go");
    DefaultInputFile nestedTestFile = getTestInputFile(fs, "\nfunc   TestFoo (", "packageFoo/foo_test.go");

    InputFile foundTestFile;
    foundTestFile = goTestSensor.findTestFile(fs, testInfoTop);
    assertThat(foundTestFile).isEqualTo(topTestFile);

    foundTestFile = goTestSensor.findTestFile(fs, testInfoNested);
    assertThat(foundTestFile).isEqualTo(nestedTestFile);
  }

  @Test
  void test_describe() {
    GoTestSensor goTestSensor = new GoTestSensor();
    DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor();
    goTestSensor.describe(descriptor);

    assertThat(descriptor.name()).isEqualTo("Go Unit Test Report");
    assertThat(descriptor.languages()).containsOnly("go");
  }

  @Test
  void import_report() {
    GoTestSensor goTestSensor = new GoTestSensor();
    goTestSensor.goPathContext = new GoPathContext(File.separatorChar, File.pathSeparator, goPath.toString());

    Path baseDir = goPath.resolve("src").resolve(packagePath);

    SensorContextTester context = SensorContextTester.create(baseDir);
    DefaultFileSystem fs = context.fileSystem();
    DefaultInputFile fooTestFile = getTestInputFile(fs, "something  \nfunc TestFoo1( \nfunc TestFoo2(  ", "foo_test.go");
    DefaultInputFile barTestFile = getTestInputFile(fs, "func TestBar(", "bar_test.go");

    MapSettings settings = new MapSettings();
    String absoluteReportPath = baseDir.resolve("report1.out").toString();
    settings.setProperty(GoTestSensor.REPORT_PATH_KEY, "report.out,invilid/report/path," + absoluteReportPath);
    context.setSettings(settings);

    goTestSensor.execute(context);

    assertThat(context.measure(fooTestFile.key(), CoreMetrics.TESTS).value()).isEqualTo(3); // one test comes from report1.out
    assertThat(context.measure(fooTestFile.key(), CoreMetrics.SKIPPED_TESTS).value()).isEqualTo(0);
    assertThat(context.measure(fooTestFile.key(), CoreMetrics.TEST_FAILURES).value()).isEqualTo(1);
    assertThat(context.measure(fooTestFile.key(), CoreMetrics.TEST_ERRORS)).isNull();
    assertThat(context.measure(fooTestFile.key(), CoreMetrics.TEST_EXECUTION_TIME).value()).isEqualTo(4);

    assertThat(context.measure(barTestFile.key(), CoreMetrics.TESTS).value()).isEqualTo(1);
    assertThat(context.measure(barTestFile.key(), CoreMetrics.SKIPPED_TESTS).value()).isEqualTo(1);
    assertThat(context.measure(barTestFile.key(), CoreMetrics.TEST_FAILURES).value()).isEqualTo(0);
    assertThat(context.measure(barTestFile.key(), CoreMetrics.TEST_ERRORS)).isNull();
    assertThat(context.measure(barTestFile.key(), CoreMetrics.TEST_EXECUTION_TIME).value()).isEqualTo(7);
    assertThat(String.join("\n", logTester.logs(LoggerLevel.ERROR)))
      .contains("Test report can't be loaded, file not found");
  }

  private DefaultInputFile getTestInputFile(DefaultFileSystem fs, String content, String relativePath) {
    DefaultInputFile nestedTestFile = new TestInputFileBuilder("moduleKey", relativePath)
      .setLanguage("go")
      .setType(Type.TEST)
      .setContents(content)
      .build();
    fs.add(nestedTestFile);
    return nestedTestFile;
  }
}
