package org.sonar.go.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.coverage.NewCoverage;
import org.sonar.api.config.Configuration;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class GoCoverageReport {

  private static final Logger LOG = Loggers.get(GoCoverageReport.class);

  public static final String REPORT_PATH_KEY = "sonar.go.coverage.reportPaths";
  public static final String DEFAULT_REPORT_PATH = "coverage.out";

  // See ParseProfiles function:
  // https://github.com/golang/go/blob/master/src/cmd/cover/profile.go
  static final Pattern MODE_LINE_REGEXP = Pattern.compile("^mode: (\\w+)$");
  static final Pattern COVERAGE_LINE_REGEXP = Pattern.compile("^(.+):(\\d+)\\.(\\d+),(\\d+)\\.(\\d+) (\\d+) (\\d+)$");

  private GoCoverageReport() {
  }

  public static void saveCoverageReports(SensorContext sensorContext, GoContext goContext) {
    try {
      Coverage coverage = new Coverage(goContext);
      for (Path reportPath : getReportPaths(sensorContext)) {
        parse(reportPath, coverage);
      }
      for (Map.Entry<String, FileCoverage> entry : coverage.fileMap.entrySet()) {
        saveFileCoverage(sensorContext, entry.getValue());
      }
    } catch (IOException | RuntimeException e) {
      LOG.error("Coverage import failed: {}", e.getMessage(), e);
    }
  }

  private static void saveFileCoverage(SensorContext sensorContext, FileCoverage fileCoverage) {
    String absolutePath = fileCoverage.absolutePath.toString();
    FileSystem fileSystem = sensorContext.fileSystem();
    InputFile inputFile = fileSystem.inputFile(fileSystem.predicates().hasAbsolutePath(absolutePath));
    if (inputFile != null) {
      LOG.debug("Saving coverage measures for file '{}'", absolutePath);
      NewCoverage newCoverage = sensorContext.newCoverage().onFile(inputFile);
      for (Map.Entry<Integer, LineCoverage> entry : fileCoverage.lineMap.entrySet()) {
        newCoverage.lineHits(entry.getKey(), entry.getValue().hits);
      }
      newCoverage.save();
    } else {
      LOG.warn("File '{}' is not included in the project, ignoring coverage", absolutePath);
    }
  }

  static List<Path> getReportPaths(SensorContext sensorContext) {
    Configuration config = sensorContext.config();
    Path baseDir = sensorContext.fileSystem().baseDir().toPath();
    Path defaultReportPath = baseDir.resolve(DEFAULT_REPORT_PATH);
    if (!config.hasKey(REPORT_PATH_KEY)) {
      if (defaultReportPath.toFile().exists()) {
        return Collections.singletonList(defaultReportPath);
      } else {
        return Collections.emptyList();
      }
    }
    String[] reportPaths = config.getStringArray(REPORT_PATH_KEY);
    List<Path> result = new ArrayList<>();
    for (String reportPath : reportPaths) {
      Path path = Paths.get(reportPath);
      if (!path.isAbsolute()) {
        path = baseDir.resolve(path);
      }
      if (path.toFile().exists()) {
        result.add(path);
      } else {
        LOG.error("Coverage report can't be loaded, file not found: '{}', ignoring this file.", path);
      }
    }
    return result;
  }

  static void parse(Path reportPath, Coverage coverage) throws IOException {
    LOG.info("Load coverage report from '{}'", reportPath);
    try (InputStream input = new FileInputStream(reportPath.toFile())) {
      Scanner scanner = new Scanner(input, UTF_8.name());
      if (!scanner.hasNextLine() || !MODE_LINE_REGEXP.matcher(scanner.nextLine()).matches()) {
        throw new IOException("Invalid go coverage, expect 'mode:' on the first line.");
      }
      int lineNumber = 2;
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        if (!line.isEmpty()) {
          coverage.add(new CoverageStat(lineNumber, line));
        }
        lineNumber++;
      }
    }
  }

  static class Coverage {
    final GoContext goContext;
    Map<String, FileCoverage> fileMap = new HashMap<>();

    Coverage(GoContext goContext) {
      this.goContext = goContext;
    }

    void add(CoverageStat coverage) {
      fileMap
        .computeIfAbsent(coverage.resolvePath(goContext), FileCoverage::new)
        .add(coverage);
    }
  }

  static class FileCoverage {
    Map<Integer, LineCoverage> lineMap = new HashMap<>();
    Path absolutePath;
    List<String> lines;

    public FileCoverage(String filePath) {
      absolutePath = Paths.get(filePath).toAbsolutePath().normalize();
      if (absolutePath.toFile().exists()) {
        try {
          lines = Files.readAllLines(absolutePath, UTF_8);
        } catch (IOException e) {
          throw new IllegalStateException(e);
        }
      }
    }

    void add(CoverageStat coverage) {
      int startLine = findStartIgnoringBrace(coverage);
      int endLine = findEndIgnoringBrace(coverage, startLine);
      for (int line = startLine; line <= endLine; line++) {
        lineMap
          .computeIfAbsent(line, key -> new LineCoverage())
          .add(coverage);
      }
    }

    int findStartIgnoringBrace(CoverageStat coverage) {
      int line = coverage.startLine;
      int column = coverage.startCol;
      while (shouldIgnore(line, column)) {
        column++;
        if (column > lines.get(line - 1).length()) {
          line++;
          column = 1;
        }
      }
      return line;
    }

    int findEndIgnoringBrace(CoverageStat coverage, int startLine) {
      int line = coverage.endLine;
      int column = coverage.endCol - 1;
      while (line > startLine && shouldIgnore(line, column)) {
        column--;
        if (column == 0) {
          line--;
          column = lines.get(line - 1).length();
        }
      }
      return line;
    }

    boolean shouldIgnore(int line, int column) {
      if (lines != null && line > 0 && line <= lines.size() && column > 0 && column <= lines.get(line - 1).length()) {
        int ch = lines.get(line - 1).charAt(column - 1);
        return ch < ' ' || ch == '{' || ch == '}';
      }
      return false;
    }
  }

  static class LineCoverage {
    int hits = 0;

    void add(CoverageStat coverage) {
      hits += coverage.count;
    }
  }

  static class CoverageStat {

    static final String LINUX_ABSOLUTE_PREFIX = "_/";
    static final String WINDOWS_ABSOLUTE_PREFIX = "_\\";
    static final Pattern WINDOWS_ABSOLUTE_REGEX = Pattern.compile("^_\\\\(\\w)_\\\\");

    final String filePath;
    final int startLine;
    final int startCol;
    final int endLine;
    final int endCol;
    final int numStmt;
    final int count;

    CoverageStat(int lineNumber, String line) {
      Matcher matcher = COVERAGE_LINE_REGEXP.matcher(line);
      if (!matcher.matches()) {
        throw new IllegalArgumentException("Invalid go coverage at line " + lineNumber);
      }
      filePath = matcher.group(1);
      startLine = Integer.parseInt(matcher.group(2));
      startCol = Integer.parseInt(matcher.group(3));
      endLine = Integer.parseInt(matcher.group(4));
      endCol = Integer.parseInt(matcher.group(5));
      numStmt = Integer.parseInt(matcher.group(6));
      count = Integer.parseInt(matcher.group(7));
    }

    String resolvePath(GoContext context) {
      if (filePath.startsWith(LINUX_ABSOLUTE_PREFIX)) {
        return filePath.substring(1);
      } else if (filePath.startsWith(WINDOWS_ABSOLUTE_PREFIX)) {
        Matcher matcher = WINDOWS_ABSOLUTE_REGEX.matcher(filePath);
        if (matcher.find()) {
          matcher.reset();
          return matcher.replaceFirst("$1:\\\\");
        }
      } else if (context.goPath != null && !context.goPath.isEmpty()) {
        return context.goPath + context.fileSeparator + filePath;
      }
      return filePath;
    }
  }

  static class GoContext {
    static final GoContext DEFAULT = new GoContext(File.separatorChar, defaultGoSrcPath());
    final char fileSeparator;
    final String goPath;

    GoContext(char fileSeparator, @Nullable String goPath) {
      this.fileSeparator = fileSeparator;
      this.goPath = goPath;
    }

    @Nullable
    static String defaultGoSrcPath() {
      String path = System.getenv("GOPATH");
      if (path != null && !path.isEmpty()) {
        return path + File.separatorChar + "src";
      }
      return null;
    }
  }
}
