package org.sonar.commonruleengine;

import java.util.ArrayList;
import java.util.List;

public class EngineContext {

  private List<Issue> issues = new ArrayList<>();

  public void reportIssue(Issue issue) {
    issues.add(issue);
  }

  public List<Issue> getIssues() {
    return issues;
  }
}

