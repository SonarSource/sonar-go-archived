package org.sonar.go.plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.go.plugin.GoCoverageReport.Coverage;
import org.sonar.go.plugin.GoCoverageReport.CoverageStat;
import org.sonar.go.plugin.GoCoverageReport.FileCoverage;
import org.sonar.go.plugin.GoCoverageReport.GoContext;
import org.sonar.go.plugin.GoCoverageReport.LineCoverage;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GoCoverageReportTest {

  static final Path COVERAGE_DIR = Paths.get("src", "test", "resources", "coverage");

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
  void coverage_stat_resolve_path() {
    CoverageStat coverage;
    GoContext linuxContext = new GoContext('/', "/home/paul/go");
    GoContext windowsContext = new GoContext('\\', "C:\\Users\\paul\\go");

    coverage = new CoverageStat(2, "_/my-app/my-app.go:2.2,2.5 1 0");
    assertThat(coverage.resolvePath(linuxContext)).isEqualTo("/my-app/my-app.go");

    coverage = new CoverageStat(2, "my-app/my-app.go:2.2,2.5 1 0");
    assertThat(coverage.resolvePath(linuxContext)).isEqualTo("/home/paul/go/my-app/my-app.go");

    coverage = new CoverageStat(2, "_\\C_\\my-app\\my-app.go:2.2,2.5 1 0");
    assertThat(coverage.resolvePath(windowsContext)).isEqualTo("C:\\my-app\\my-app.go");

    coverage = new CoverageStat(2, "my-app\\my-app.go:2.2,2.5 1 0");
    assertThat(coverage.resolvePath(windowsContext)).isEqualTo("C:\\Users\\paul\\go\\my-app\\my-app.go");

    linuxContext = new GoContext('/', null);
    coverage = new CoverageStat(2, "my-app/my-app.go:2.2,2.5 1 0");
    assertThat(coverage.resolvePath(linuxContext)).isEqualTo("my-app/my-app.go");
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
  void file_coverage() {
    FileCoverage file = new FileCoverage(COVERAGE_DIR.resolve("cover.go").toString());
    assertThat(file.lineMap.size()).isEqualTo(0);
    file.add(new CoverageStat(2, "cover.go:4.11,6.3 1 3"));
    file.add(new CoverageStat(3, "cover.go:6.3,8.3 1 0"));
    assertThat(file.lineMap.keySet()).containsExactlyInAnyOrder(5, 6, 7);
    assertThat(file.lineMap.get(4)).isNull();
    assertThat(file.lineMap.get(5).hits).isEqualTo(3);
    assertThat(file.lineMap.get(6).hits).isEqualTo(0);
    assertThat(file.lineMap.get(7).hits).isEqualTo(0);
    assertThat(file.lineMap.get(8)).isNull();
  }

  @Test
  void coverage() {
    GoContext linuxContext = new GoContext('/', "/home/paul/go");
    Coverage coverage = new Coverage(linuxContext);
    coverage.add(new CoverageStat(2, "main.go:2.2,2.5 1 1"));
    coverage.add(new CoverageStat(3, "main.go:4.2,4.7 1 0"));
    coverage.add(new CoverageStat(4, "other.go:3.2,4.12 1 1"));
    assertThat(coverage.fileMap.keySet()).containsExactlyInAnyOrder("/home/paul/go/main.go", "/home/paul/go/other.go");
    assertThat(coverage.fileMap.get("/home/paul/go/main.go").lineMap.keySet()).containsExactlyInAnyOrder(2, 4);
    assertThat(coverage.fileMap.get("/home/paul/go/other.go").lineMap.keySet()).containsExactlyInAnyOrder(3, 4);
  }

  @Test
  void parse_coverage_linux_relative() throws IOException {
    Path coverageFile = COVERAGE_DIR.resolve("coverage.linux.relative.out");
    GoContext linuxContext = new GoContext('/', "/home/paul/go");
    String coverPath = "/home/paul/go/github.com/SonarSource/sonar-go/sonar-go-plugin/src/test/resources/coverage/cover.go";
    assertCoverGo(coverageFile, linuxContext, coverPath);
  }

  @Test
  void parse_coverage_linux_absolute() throws IOException {
    Path coverageFile = COVERAGE_DIR.resolve("coverage.linux.absolute.out");
    GoContext linuxContext = new GoContext('/', "/home/paul/go");
    String coverPath = "/home/paul/dev/github/SonarSource/sonar-go/sonar-go-plugin/src/test/resources/coverage/cover.go";
    assertCoverGo(coverageFile, linuxContext, coverPath);
  }

  @Test
  void parse_coverage_windows_relative() throws IOException {
    Path coverageFile = COVERAGE_DIR.resolve("coverage.win.relative.out");
    GoContext windowsContext = new GoContext('\\', "C:\\Users\\paul\\go");
    String coverPath = "C:\\Users\\paul\\go\\github.com\\SonarSource\\sonar-go\\sonar-go-plugin\\src\\test\\resources\\coverage\\cover.go";
    assertCoverGo(coverageFile, windowsContext, coverPath);
  }

  @Test
  void parse_coverage_windows_absolute() throws IOException {
    Path coverageFile = COVERAGE_DIR.resolve("coverage.win.absolute.out");
    GoContext windowsContext = new GoContext('\\', "C:\\Users\\paul\\go");
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
    List<Path> reportPaths = GoCoverageReport.getReportPaths(context);
    assertThat(reportPaths).containsExactlyInAnyOrder(
      coverageFile1,
      Paths.get("src", "test", "resources", "coverage", "coverage.linux.absolute.out"));
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
      .build());
    GoContext goContext = new GoContext(File.separatorChar, COVERAGE_DIR.toString());
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

  void assertCoverGo(Path coverageFile, GoContext goContext, String absolutePath) throws IOException {
    Coverage coverage = new Coverage(goContext);
    GoCoverageReport.parse(coverageFile, coverage);
    assertThat(coverage.fileMap.keySet()).containsExactlyInAnyOrder(absolutePath);
    FileCoverage fileCoverage = coverage.fileMap.get(absolutePath);
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
