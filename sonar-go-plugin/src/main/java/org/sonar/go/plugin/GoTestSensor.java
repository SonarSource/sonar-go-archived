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

import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class GoTestSensor implements Sensor {

  private static final Logger LOG = Loggers.get(GoTestSensor.class);

  public static final String REPORT_PATH_KEY = "sonar.go.tests.reportPaths";
  private static final Gson GSON = new Gson();

  GoPathContext goPathContext = GoPathContext.DEFAULT;

  // caching package <-> test input files
  private Map<String, List<InputFile>> testFilesByPackage = new HashMap<>();

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor.onlyOnLanguage(GoLanguage.KEY)
      .onlyWhenConfiguration(conf -> conf.hasKey(REPORT_PATH_KEY))
      .name("Go Unit Test Report");
  }

  @Override
  public void execute(SensorContext context) {
    Map<InputFile, List<TestInfo>> testInfoByFile = new HashMap<>();

    getReportPaths(context).forEach(path -> parseReport(context, path, testInfoByFile));
    testInfoByFile.forEach((key, value) -> saveTestMetrics(context, key, value));
  }

  private static List<Path> getReportPaths(SensorContext context) {
    List<Path> result = new ArrayList<>();
    String[] reportPaths = context.config().getStringArray(REPORT_PATH_KEY);
    for (String reportPath : reportPaths) {
      Path path = Paths.get(reportPath);
      if (!path.isAbsolute()) {
        path = context.fileSystem().baseDir().toPath().resolve(path);
      }
      if (path.toFile().exists()) {
        result.add(path);
      } else {
        LOG.error("Coverage report can't be loaded, file not found: '{}', ignoring this file.", path);
      }
    }

    return result;
  }

  private void parseReport(SensorContext context, Path reportPath, Map<InputFile, List<TestInfo>> testInfoByFile) {
    try {
      List<TestInfo> testInfoList = Files.readAllLines(reportPath).stream()
        .filter(line -> line.startsWith("{"))
        .map(line -> getRelevantTestInfo(line, reportPath))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());

      for (TestInfo testInfo : testInfoList) {
        InputFile testFile = findTestFile(context.fileSystem(), testInfo);

        if (testFile != null) {
          testInfoByFile
            .computeIfAbsent(testFile, key -> new ArrayList<>())
            .add(testInfo);
        } else {
          LOG.warn("Failed to find test file for package " + testInfo.Package + " and test " + testInfo.Test);
        }
      }
    } catch (IOException e) {
      LOG.error("Failed to read unit test report file " + reportPath.toString(), e);
    }
  }

  @Nullable
  private static TestInfo getRelevantTestInfo(String line, Path reportPath) {
    try {
      TestInfo testInfo = GSON.fromJson(line, TestInfo.class);
      if (testInfo.isRelevant()) {
        return testInfo;
      }
    } catch (Exception e) {
      LOG.error("Failed to parse unit test report line (file " + reportPath + "):\n " + line);
    }

    return null;
  }

  @Nullable
  InputFile findTestFile(FileSystem fileSystem, TestInfo testInfo) throws IOException {
    List<InputFile> testInputFilesInPackage = testFilesByPackage.computeIfAbsent(
      testInfo.Package,
      goPackage -> getTestFilesForPackage(fileSystem, goPackage));

    Pattern pattern = Pattern.compile("^func\\s+" + testInfo.Test + "\\s*\\(", Pattern.MULTILINE);
    for (InputFile testFile : testInputFilesInPackage) {
      if (pattern.matcher(testFile.contents()).find()) {
        return testFile;
      }
    }

    return null;
  }

  private List<InputFile> getTestFilesForPackage(FileSystem fileSystem, String goPackage) {
    FilePredicates predicates = fileSystem.predicates();
    String packageDirectory = goPathContext.resolve(goPackage);

    if (!new File(packageDirectory).exists()) {
      packageDirectory = findPackageDirectory(goPackage, fileSystem);
      if (packageDirectory == null) {
        return Collections.emptyList();
      }
    }

    try (Stream<Path> stream = Files.list(Paths.get(packageDirectory))){
      return stream
        .map(path -> fileSystem.inputFile(testFilePredicate(predicates, path)))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());

    } catch (IOException e) {
      LOG.warn("Failed to read package directory " + packageDirectory, e);
      return Collections.emptyList();
    }
  }

  private static FilePredicate testFilePredicate(FilePredicates predicates, Path path) {
    return predicates.and(
      predicates.hasType(Type.TEST),
      predicates.hasAbsolutePath(path.toString()),
      predicates.hasLanguage(GoLanguage.KEY));
  }

  private static String findPackageDirectory(String packagePath, FileSystem fileSystem) {
    File resolved = fileSystem.baseDir().toPath().resolve(packagePath).toFile();
    if (resolved.exists()) {
      return resolved.toString();
    }

    Path path = Paths.get(packagePath);
    if (path.getNameCount() == 1) {
      if (path.getName(0).toString().equals(fileSystem.baseDir().getName())) {
        return fileSystem.baseDir().toString();
      } else {
        return null;
      }
    } else {
      Path subpath = path.subpath(1, path.getNameCount());
      return findPackageDirectory(subpath.toString(), fileSystem);
    }
  }


  private static void saveTestMetrics(SensorContext context, InputFile testFile, List<TestInfo> tests) {
    int skip = 0;
    long timeMs = 0;
    int fail = 0;
    for (TestInfo test : tests) {
      timeMs += test.Elapsed * 1000;
      if (test.Action.equals("skip")) {
        skip++;
      } else if (test.Action.equals("fail")) {
        fail++;
      }
    }

    context.<Integer>newMeasure().on(testFile).withValue(skip).forMetric(CoreMetrics.SKIPPED_TESTS).save();
    context.<Long>newMeasure().on(testFile).withValue(timeMs).forMetric(CoreMetrics.TEST_EXECUTION_TIME).save();
    context.<Integer>newMeasure().on(testFile).withValue(tests.size()).forMetric(CoreMetrics.TESTS).save();
    context.<Integer>newMeasure().on(testFile).withValue(fail).forMetric(CoreMetrics.TEST_FAILURES).save();
  }

  static class TestInfo {
    String Action;
    String Package;
    String Test;
    Double Elapsed;

    TestInfo(String action, String aPackage, String test, Double elapsed) {
      Action = action;
      Package = aPackage;
      Test = test;
      Elapsed = elapsed;
    }

    boolean isRelevant() {
      return Action != null && Test != null && Package != null &&
        (Action.equals("pass") || Action.equals("fail") || Action.equals("skip"));
    }
  }
}
