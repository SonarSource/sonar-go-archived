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
package com.sonar.it.go;

import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.build.SonarScanner;
import com.sonar.orchestrator.container.Server;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.ClassRule;
import org.junit.Test;
import org.sonar.wsclient.SonarClient;
import org.sonar.wsclient.issue.Issue;
import org.sonar.wsclient.issue.IssueClient;
import org.sonar.wsclient.issue.IssueQuery;

import static org.assertj.core.api.Assertions.assertThat;

public class GoVetReportTest {

  @ClassRule
  public static Orchestrator orchestrator = Tests.ORCHESTRATOR;

  private static final String PROJECT_KEY = "project-id";

  @Test
  public void should_import_go_vet_issues() {
    SonarScanner build = SonarScanner.create()
      .setProjectKey(PROJECT_KEY)
      .setProjectName("Import go vet test")
      .setProjectVersion("1")
      .setSourceEncoding("UTF-8")
      .setSourceDirs(".")
      .setProjectDir(Tests.projectDirectoryFor("samples"))
      .setProperty("sonar.go.govet.reportPaths", "go-vet.out");
    orchestrator.executeBuild(build);

    Server server = orchestrator.getServer();
    IssueClient issueClient = SonarClient.create(server.getUrl()).issueClient();
    List<Issue> issues = issueClient.find(IssueQuery.create().componentRoots(PROJECT_KEY)).list();

    if (orchestrator.getServer().version().isGreaterThanOrEquals("7.2")) {
      assertThat(issues).hasSize(16);
      String issueReport = issues.stream()
        .filter(issue -> !issue.ruleKey().startsWith("go:")) // only keep external issues
        .map(issue -> issue.componentKey() + "|" +
          issue.ruleKey() + "|" +
          issue.severity() + "|" +
          issue.debt() + "|" +
          "line:" + issue.line() + "|" +
          issue.message())
        .sorted()
        .collect(Collectors.joining("\n"));

      assertThat(issueReport).isEqualTo(
        "project-id:SelfAssignement.go|external_govet:generic|MAJOR|5min|line:7|self-assignment of name to name\n" +
        "project-id:SelfAssignement.go|external_govet:generic|MAJOR|5min|line:9|self-assignment of user.name to user.name");
    } else {
      assertThat(issues).hasSize(14);
    }
  }

}
