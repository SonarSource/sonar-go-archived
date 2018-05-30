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
package org.sonar.go.plugin.externalreport;

import java.io.File;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.Severity;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewExternalIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rules.RuleType;
import org.sonar.api.utils.Version;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.go.plugin.GoLanguage;

public abstract class AbstractReportSensor implements Sensor {

  private static final Logger LOG = Loggers.get(AbstractReportSensor.class);

  static final long DEFAULT_REMEDIATION_COST = 5L;
  static final Severity DEFAULT_SEVERITY = Severity.MAJOR;

  abstract String linterId();

  abstract String linterName();

  abstract String reportsPropertyName();

  abstract void importReport(File report, SensorContext context);

  @Override
  public void execute(SensorContext context) {
    boolean externalIssuesSupported = context.getSonarQubeVersion().isGreaterThanOrEqual(Version.create(7, 2));
    String[] reportPaths = context.config().getStringArray(reportsPropertyName());
    if (reportPaths.length == 0) {
      return;
    }

    if (!externalIssuesSupported) {
      LOG.error("Import of external issues requires SonarQube 7.2 or greater.");
      return;
    }

    for (String reportPath : reportPaths) {
      File report = getIOFile(context.fileSystem().baseDir(), reportPath);
      importReport(report, context);
    }
  }

  @Override
  public void describe(SensorDescriptor sensorDescriptor) {
    sensorDescriptor
      .onlyOnLanguage(GoLanguage.KEY)
      .name("Import of " + linterName() + " issues");
  }

  /**
    * Returns a java.io.File for the given path.
    * If path is not absolute, returns a File with module base directory as parent path.
    */
  static File getIOFile(File baseDir, String path) {
    File file = new File(path);
    if (!file.isAbsolute()) {
      file = new File(baseDir, path);
    }
    return file;
  }

  InputFile getInputFile(SensorContext context, String filePath) {
    FilePredicates predicates = context.fileSystem().predicates();
    InputFile inputFile = context.fileSystem().inputFile(predicates.or(predicates.hasRelativePath(filePath), predicates.hasAbsolutePath(filePath)));
    if (inputFile == null) {
      LOG.warn("No input file found for {}. No {} issues will be imported on this file.", filePath, linterName());
      return null;
    }
    return inputFile;
  }

  void addLineIssue(SensorContext context, String filePath, int line, String message) {
    InputFile inputFile = getInputFile(context, filePath);
    if (inputFile != null) {
      NewExternalIssue newExternalIssue = context.newExternalIssue();
      NewIssueLocation primaryLocation = newExternalIssue.newLocation()
        .message(message)
        .on(inputFile)
        .at(inputFile.selectLine(line));

      newExternalIssue
        .at(primaryLocation)
        .forRule(RuleKey.of(linterId(), "generic"))
        .type(RuleType.BUG)
        .severity(DEFAULT_SEVERITY)
        .remediationEffortMinutes(DEFAULT_REMEDIATION_COST)
        .save();
    }
  }
}
