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
package com.sonar.it.go;

import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.build.SonarScanner;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.ClassRule;
import org.junit.Test;
import org.sonarqube.ws.Issues;
import org.sonarqube.ws.client.issues.SearchRequest;

import static com.sonar.it.go.Tests.newWsClient;
import static org.assertj.core.api.Assertions.assertThat;

public class ExternalLinterReportTest {

  @ClassRule
  public static Orchestrator orchestrator = Tests.ORCHESTRATOR;

  @Test
  public void should_import_go_vet_issues() {
    String projectKey = "go-vet-project";
    orchestrator.executeBuild(createBuild(projectKey,
      "sonar.go.govet.reportPaths", "go-vet.out"));
    List<Issues.Issue> issues = getExternalIssues(projectKey);
    if (orchestrator.getServer().version().isGreaterThanOrEquals(7, 2)) {
      assertThat(issues).hasSize(2);
      assertThat(formatIssues(issues)).isEqualTo(
        "SelfAssignement.go|external_govet:assign|MAJOR|5min|line:7|self-assignment of name to name\n" +
          "SelfAssignement.go|external_govet:assign|MAJOR|5min|line:9|self-assignment of user.name to user.name");
    } else {
      assertThat(issues).isEmpty();
    }
  }

  @Test
  public void should_import_go_lint_issues() {
    String projectKey = "golint-project";
    orchestrator.executeBuild(createBuild(projectKey,
      "sonar.go.golint.reportPaths", "golint.out"));
    List<Issues.Issue> issues = getExternalIssues(projectKey);
    if (orchestrator.getServer().version().isGreaterThanOrEquals(7, 2)) {
      assertThat(issues).hasSize(11);
      assertThat(formatIssues(issues)).isEqualTo(
        "SelfAssignement.go|external_golint:Exported|MAJOR|5min|line:4|exported type User should have comment or be unexported\n" +
          "SelfAssignement.go|external_golint:PackageComment|MAJOR|5min|line:1|package comment should be of the form \"Package samples ...\"\n" +
          "TabCharacter.go|external_golint:PackageComment|MAJOR|5min|line:1|package comment should be of the form \"Package samples ...\"\n" +
          "TodoTagPresence.go|external_golint:PackageComment|MAJOR|5min|line:1|package comment should be of the form \"Package samples ...\"\n" +
          "TooLongLine.go|external_golint:PackageComment|MAJOR|5min|line:1|package comment should be of the form \"Package samples ...\"\n" +
          "TooManyParameters.go|external_golint:PackageComment|MAJOR|5min|line:1|package comment should be of the form \"Package samples ...\"\n" +
          "pivot.go|external_golint:Names|MAJOR|5min|line:10|don't use underscores in Go names; var ascii_uppercase should be asciiUppercase\n" +
          "pivot.go|external_golint:Names|MAJOR|5min|line:11|don't use underscores in Go names; var ascii_lowercase should be asciiLowercase\n" +
          "pivot.go|external_golint:Names|MAJOR|5min|line:12|don't use underscores in Go names; var ascii_uppercase_len should be asciiUppercaseLen\n" +
          "pivot.go|external_golint:Names|MAJOR|5min|line:13|don't use underscores in Go names; var ascii_lowercase_len should be asciiLowercaseLen\n" +
          "pivot.go|external_golint:Names|MAJOR|5min|line:14|don't use underscores in Go names; var ascii_allowed should be asciiAllowed"
      );
    } else {
      assertThat(issues).isEmpty();
    }
  }

  @Test
  public void should_import_gometalinter_issues() {
    String projectKey = "gometalinter-project";
    orchestrator.executeBuild(createBuild(projectKey,
      "sonar.go.gometalinter.reportPaths", "gometalinter.out"));
    List<Issues.Issue> issues = getExternalIssues(projectKey);
    if (orchestrator.getServer().version().isGreaterThanOrEquals(7, 2)) {
      assertThat(issues).hasSize(8);
      assertThat(formatIssues(issues)).isEqualTo(
        "SelfAssignement.go|external_golint:Exported|MAJOR|5min|line:4|exported type User should have comment or be unexported\n" +
          "SelfAssignement.go|external_golint:PackageComment|MAJOR|5min|line:1|package comment should be of the form \"Package samples ...\"\n" +
          "SelfAssignement.go|external_govet:assign|MAJOR|5min|line:7|self-assignment of name to name\n" +
          "SelfAssignement.go|external_govet:assign|MAJOR|5min|line:9|self-assignment of user.name to user.name\n" +
          "SelfAssignement.go|external_megacheck:SA4018|MAJOR|5min|line:7|self-assignment of name to name\n" +
          "SelfAssignement.go|external_megacheck:SA4018|MAJOR|5min|line:9|self-assignment of user.name to user.name\n" +
          "SelfAssignement.go|external_megacheck:U1000|MAJOR|5min|line:4|field name is unused\n" +
          "SelfAssignement.go|external_megacheck:U1000|MAJOR|5min|line:6|func (*User).rename is unused");
    } else {
      assertThat(issues).isEmpty();
    }
  }

  private static SonarScanner createBuild(String projectKey, String propertyName, String propertyValue) {
    return SonarScanner.create()
      .setProjectKey(projectKey)
      .setProjectName("project-name")
      .setProjectVersion("1")
      .setSourceEncoding("UTF-8")
      .setSourceDirs(".")
      .setProperty(propertyName, propertyValue)
      .setProjectDir(Tests.projectDirectoryFor("samples"));
  }

  private static List<Issues.Issue> getExternalIssues(String projectKey) {
    SearchRequest request = new SearchRequest();
    request.setProjects(Collections.singletonList(projectKey));
    return newWsClient().issues().search(request).getIssuesList().stream()
      .filter(issue -> !issue.getExternalRuleEngine().isEmpty())
      .collect(Collectors.toList());
  }

  private static String formatIssues(List<Issues.Issue> issues) {
    return issues.stream()
      .map(issue -> filePath(issue) + "|" +
        issue.getRule() + "|" +
        issue.getSeverity() + "|" +
        issue.getDebt() + "|" +
        "line:" + issue.getLine() + "|" +
        issue.getMessage())
      .sorted()
      .collect(Collectors.joining("\n"));
  }

  private static String filePath(Issues.Issue issue) {
    return issue.getComponent().substring(issue.getComponent().indexOf(':') + 1);
  }

}
