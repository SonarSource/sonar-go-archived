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
package org.sonar.go.plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.utils.log.LoggerLevel;
import org.sonar.go.plugin.GoCoverageReport.Coverage;
import org.sonar.go.plugin.GoCoverageReport.CoverageStat;
import org.sonar.go.plugin.GoCoverageReport.FileCoverage;
import org.sonar.go.plugin.GoCoverageReport.LineCoverage;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GoCoverageReportTest {

  static final Path COVERAGE_DIR = Paths.get("src", "test", "resources", "coverage");

  @RegisterExtension
  static JUnit5LogTester logTester = new JUnit5LogTester();

  @Test
  void mode_line() {
    Predicate<String> regexp = (line) -> GoCoverageReport.MODE_LINE_REGEXP.matcher(line).matches();
    assertThat(regexp.test("mode: set")).isTrue();
    assertThat(regexp.test("mode: count")).isTrue();
    assertThat(regexp.test("mode: atomic")).isTrue();
    assertThat(regexp.test("my-app/my-app.go:3.2,3.10 1 1")).isFalse();
  }

  @Test
  void line_regexp() {
    Predicate<String> regexp = (line) -> GoCoverageReport.COVERAGE_LINE_REGEXP.matcher(line).matches();
    assertThat(regexp.test("my-app/my-app.go:3.2,3.10 1 1")).isTrue();
    assertThat(regexp.test("_/my-app/my-app.go:3.2,3.10 1 21")).isTrue();
    assertThat(regexp.test("my-app\\my-app.go:3.2,3.10 1 0")).isTrue();
    assertThat(regexp.test("_\\C_\\my-app\\my-app.go:3.2,3.10 1 42")).isTrue();
    assertThat(regexp.test("mode: set")).isFalse();
  }

  @Test
  void coverage_stat() {
    CoverageStat coverage = new CoverageStat(2, "_/my-app/my-app.go:3.10,4.5 2 234");
    assertThat(coverage.filePath).isEqualTo("_/my-app/my-app.go");
    assertThat(coverage.startLine).isEqualTo(3);
    assertThat(coverage.startCol).isEqualTo(10);
    assertThat(coverage.endLine).isEqualTo(4);
    assertThat(coverage.endCol).isEqualTo(5);
    assertThat(coverage.numStmt).isEqualTo(2);
    assertThat(coverage.count).isEqualTo(234);

    assertThat(
      assertThrows(IllegalArgumentException.class, () -> new CoverageStat(42, "invalid"))
        .getMessage()).isEqualTo("Invalid go coverage at line 42");
  }

  @Test
  void line_coverage() {
    LineCoverage line = new LineCoverage();
    assertThat(line.hits).isEqualTo(0);

    line.add(new CoverageStat(2, "main.go:2.2,2.5 1 0"));
    assertThat(line.hits).isEqualTo(0);

    line.add(new CoverageStat(2, "main.go:2.2,2.5 1 3"));
    assertThat(line.hits).isEqualTo(3);

    line.add(new CoverageStat(2, "main.go:2.2,2.5 1 2"));
    assertThat(line.hits).isEqualTo(5);

    line.add(new CoverageStat(2, "main.go:2.8,2.10 1 0"));
    assertThat(line.hits).isEqualTo(5);
  }

  @Test
  void file_coverage() throws Exception {
    List<CoverageStat> coverageStats = Arrays.asList(
      new CoverageStat(2, "cover.go:4.11,6.3 1 3"),
      new CoverageStat(3, "cover.go:6.3,8.3 1 0"));
    FileCoverage file = new FileCoverage(coverageStats, Files.readAllLines(COVERAGE_DIR.resolve("cover.go")));

    assertThat(file.lineMap.keySet()).containsExactlyInAnyOrder(5, 6, 7);
    assertThat(file.lineMap.get(4)).isNull();
    assertThat(file.lineMap.get(5).hits).isEqualTo(3);
    assertThat(file.lineMap.get(6).hits).isEqualTo(0);
    assertThat(file.lineMap.get(7).hits).isEqualTo(0);
    assertThat(file.lineMap.get(8)).isNull();
  }

  @Test
  void coverage() {
    GoPathContext linuxContext = new GoPathContext('/', ":", "/home/paul/go");
    Coverage coverage = new Coverage(linuxContext);
    coverage.add(new CoverageStat(2, "main.go:2.2,2.5 1 1"));
    coverage.add(new CoverageStat(3, "main.go:4.2,4.7 1 0"));
    coverage.add(new CoverageStat(4, "other.go:3.2,4.12 1 1"));
    assertThat(coverage.fileMap.keySet()).containsExactlyInAnyOrder("/home/paul/go/src/main.go", "/home/paul/go/src/other.go");
    List<CoverageStat> coverageStats = coverage.fileMap.get("/home/paul/go/src/main.go");
    FileCoverage fileCoverage = new FileCoverage(coverageStats, null);
    assertThat(fileCoverage.lineMap.keySet()).containsExactlyInAnyOrder(2, 4);
    assertThat(new FileCoverage(coverage.fileMap.get("/home/paul/go/src/other.go"), null).lineMap.keySet()).containsExactlyInAnyOrder(3, 4);
  }

  @Test
  void parse_coverage_linux_relative() throws IOException {
    Path coverageFile = COVERAGE_DIR.resolve("coverage.linux.relative.out");
    GoPathContext linuxContext = new GoPathContext('/', ":", "/home/paul/go");
    String coverPath = "/home/paul/go/src/github.com/SonarSource/sonar-go/sonar-go-plugin/src/test/resources/coverage/cover.go";
    assertCoverGo(coverageFile, linuxContext, coverPath);
  }

  @Test
  void parse_coverage_linux_absolute() throws IOException {
    Path coverageFile = COVERAGE_DIR.resolve("coverage.linux.absolute.out");
    GoPathContext linuxContext = new GoPathContext('/', ":", "/home/paul/go");
    String coverPath = "/home/paul/dev/github/SonarSource/sonar-go/sonar-go-plugin/src/test/resources/coverage/cover.go";
    assertCoverGo(coverageFile, linuxContext, coverPath);
  }

  @Test
  void parse_coverage_windows_relative() throws IOException {
    Path coverageFile = COVERAGE_DIR.resolve("coverage.win.relative.out");
    GoPathContext windowsContext = new GoPathContext('\\', ";", "C:\\Users\\paul\\go");
    String coverPath = "C:\\Users\\paul\\go\\src\\github.com\\SonarSource\\sonar-go\\sonar-go-plugin\\src\\test\\resources\\coverage\\cover.go";
    assertCoverGo(coverageFile, windowsContext, coverPath);
  }

  @Test
  void parse_coverage_windows_absolute() throws IOException {
    Path coverageFile = COVERAGE_DIR.resolve("coverage.win.absolute.out");
    GoPathContext windowsContext = new GoPathContext('\\', ";", "C:\\Users\\paul\\go");
    String coverPath = "C:\\Users\\paul\\dev\\github\\SonarSource\\sonar-go\\sonar-go-plugin\\src\\test\\resources\\coverage\\cover.go";
    assertCoverGo(coverageFile, windowsContext, coverPath);
  }

  @Test
  public void get_report_paths() {
    SensorContextTester context = SensorContextTester.create(COVERAGE_DIR);
    context.setSettings(new MapSettings());
    Path coverageFile1 = COVERAGE_DIR.resolve("coverage.linux.relative.out").toAbsolutePath();
    context.settings().setProperty("sonar.go.coverage.reportPaths",
      coverageFile1 + ",coverage.linux.absolute.out");
    Stream<Path> reportPaths = GoCoverageReport.getReportPaths(context);
    assertThat(reportPaths).containsExactlyInAnyOrder(
      coverageFile1,
      Paths.get("src", "test", "resources", "coverage", "coverage.linux.absolute.out"));
  }

  @Test
  public void get_report_paths_with_wildcards() {
    SensorContextTester context = SensorContextTester.create(COVERAGE_DIR);
    context.setSettings(new MapSettings());
    context.settings().setProperty("sonar.go.coverage.reportPaths",
      "*.absolute.out,glob/*.out, test*/*.out, coverage?.out");
    Stream<Path> reportPaths = GoCoverageReport.getReportPaths(context);
    assertThat(reportPaths).containsExactlyInAnyOrder(
      Paths.get("src", "test", "resources", "coverage", "coverage.linux.absolute.out"),
      Paths.get("src", "test", "resources", "coverage", "coverage.win.absolute.out"),
      Paths.get("src", "test", "resources", "coverage", "glob", "coverage.glob.out"),
      Paths.get("src", "test", "resources", "coverage", "test1", "coverage.out"),
      Paths.get("src", "test", "resources", "coverage", "coverage1.out"));
  }

  @Test
  public void should_continue_if_parsing_fails() {
    SensorContextTester context = SensorContextTester.create(COVERAGE_DIR);
    context.setSettings(new MapSettings());
    context.settings().setProperty("sonar.go.coverage.reportPaths",
      "test1/coverage.out, coverage.relative.out");
    Path baseDir = COVERAGE_DIR.toAbsolutePath();
    GoPathContext goContext = new GoPathContext(File.separatorChar, File.pathSeparator, baseDir.toString());
    GoCoverageReport.saveCoverageReports(context, goContext);
    String errorMessageForInvalidFile = "Error parsing coverage info for file src/test/resources/coverage/test1/coverage.out: Invalid go coverage, expect 'mode:' on the first line.";
    String errorMessageForValidFile = "Error parsing coverage info for file src/test/resources/coverage/coverage.relative.out: Invalid go coverage, expect 'mode:' on the first line.";
    assertThat(logTester.logs(LoggerLevel.ERROR)).contains(errorMessageForInvalidFile).doesNotContain(errorMessageForValidFile);
  }

  @Test
  void upload_reports() throws IOException {
    Path baseDir = COVERAGE_DIR.toAbsolutePath();
    SensorContextTester context = SensorContextTester.create(baseDir);
    context.setSettings(new MapSettings());
    context.settings().setProperty("sonar.go.coverage.reportPaths", "coverage.relative.out");
    Path goFilePath = baseDir.resolve("cover.go");
    String content = new String(Files.readAllBytes(goFilePath), UTF_8);
    context.fileSystem().add(TestInputFileBuilder.create("moduleKey", baseDir.toFile(), goFilePath.toFile())
      .setLanguage("go")
      .setType(InputFile.Type.MAIN)
      .initMetadata(content)
      .setContents(content)
      .build());
    GoPathContext goContext = new GoPathContext(File.separatorChar, File.pathSeparator, baseDir.toString());
    GoCoverageReport.saveCoverageReports(context, goContext);
    String fileKey = "moduleKey:cover.go";
    assertThat(context.lineHits(fileKey, 3)).isNull();
    assertThat(context.lineHits(fileKey, 4)).isEqualTo(1);
    assertThat(context.lineHits(fileKey, 5)).isEqualTo(2);
    assertThat(context.conditions(fileKey, 5)).isNull();
    assertThat(context.coveredConditions(fileKey, 5)).isNull();
    assertThat(context.lineHits(fileKey, 6)).isEqualTo(0);
    assertThat(context.lineHits(fileKey, 7)).isEqualTo(0);
    assertThat(context.lineHits(fileKey, 8)).isNull();
  }

  @Test
  void coverage_fuzzy_inputfile() throws Exception {
    Path baseDir = COVERAGE_DIR.toAbsolutePath();
    SensorContextTester context = SensorContextTester.create(baseDir);
    context.setSettings(new MapSettings());
    context.settings().setProperty("sonar.go.coverage.reportPaths", "coverage.fuzzy.out");
    Path goFilePath = baseDir.resolve("cover.go");
    String content = new String(Files.readAllBytes(goFilePath), UTF_8);
    context.fileSystem().add(TestInputFileBuilder.create("moduleKey", baseDir.toFile(), goFilePath.toFile())
      .setLanguage("go")
      .setType(InputFile.Type.MAIN)
      .initMetadata(content)
      .setContents(content)
      .build());
    GoPathContext goContext = new GoPathContext(File.separatorChar, File.pathSeparator, "");
    GoCoverageReport.saveCoverageReports(context, goContext);
    String fileKey = "moduleKey:cover.go";
    assertThat(context.lineHits(fileKey, 3)).isNull();
    assertThat(context.lineHits(fileKey, 4)).isEqualTo(1);
    assertThat(context.lineHits(fileKey, 5)).isEqualTo(2);
    assertThat(context.conditions(fileKey, 5)).isNull();
    assertThat(context.coveredConditions(fileKey, 5)).isNull();
    assertThat(context.lineHits(fileKey, 6)).isEqualTo(0);
    assertThat(context.lineHits(fileKey, 7)).isEqualTo(0);
    assertThat(context.lineHits(fileKey, 8)).isNull();

    String ignoredFileLog = "File 'doesntexists.go' is not included in the project, ignoring coverage";
    assertThat(logTester.logs(LoggerLevel.WARN)).contains(ignoredFileLog);
  }

  void assertCoverGo(Path coverageFile, GoPathContext goContext, String absolutePath) throws IOException {
    Coverage coverage = new Coverage(goContext);
    GoCoverageReport.parse(coverageFile, coverage);
    assertThat(coverage.fileMap.keySet()).containsExactlyInAnyOrder(absolutePath);
    List<CoverageStat> coverageStats = coverage.fileMap.get(absolutePath);
    FileCoverage fileCoverage = new FileCoverage(coverageStats, null);
    assertThat(fileCoverage.lineMap.keySet()).containsExactlyInAnyOrder(3, 4, 5, 6, 7, 8);
    assertThat(fileCoverage.lineMap.get(2)).isNull();
    assertThat(fileCoverage.lineMap.get(3).hits).isEqualTo(1);
    assertThat(fileCoverage.lineMap.get(4).hits).isEqualTo(2);
    assertThat(fileCoverage.lineMap.get(5).hits).isEqualTo(2);
    assertThat(fileCoverage.lineMap.get(6).hits).isEqualTo(0);
    assertThat(fileCoverage.lineMap.get(7).hits).isEqualTo(0);
    assertThat(fileCoverage.lineMap.get(8).hits).isEqualTo(0);
    assertThat(fileCoverage.lineMap.get(9)).isNull();
  }

}
