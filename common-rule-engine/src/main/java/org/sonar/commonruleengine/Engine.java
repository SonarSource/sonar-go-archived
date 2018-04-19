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
package org.sonar.commonruleengine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.commonruleengine.checks.Check;
import org.sonar.uast.UastNode;
import org.sonar.uast.validators.Validator;
import org.sonar.uast.validators.ValidatorList;

public class Engine {

  private final EngineContext engineContext;
  private final MetricsVisitor metricsVisitor;

  public Engine(Collection<Check> rules) {
    this(rules, ValidatorList.all());
  }

  public Engine(Collection<Check> rules, Collection<Validator> validators) {
    engineContext = new EngineContext();
    metricsVisitor = new MetricsVisitor();
    // should this be parameterized in order to switch ON/OFF validation?
    Collection<Check> rulesWithValidators = new ArrayList<>();
    // add validators first, to validate before playing any rule
    rulesWithValidators.addAll(validators);
    rulesWithValidators.addAll(rules);
    rulesWithValidators.forEach(rule -> rule.initialize(engineContext));
  }


  public ScanResult scan(UastNode uast, InputFile inputFile) throws IOException {
    metricsVisitor.enterFile(uast);
    engineContext.enterFile(inputFile);
    visit(uast);
    return new ScanResult(engineContext.getIssues(), metricsVisitor.getMetrics());
  }

  private void visit(UastNode uast) {
    metricsVisitor.visitNode(uast);
    Set<Check> checks = uast.kinds.stream()
      .flatMap(kind -> engineContext.registeredChecks(kind).stream())
      .collect(Collectors.toSet());
    for (Check check : checks) {
      check.visitNode(uast);
    }
    for (UastNode child : uast.children) {
      visit(child);
    }
    for (Check check : checks) {
      check.leaveNode(uast);
    }
  }

  public static class ScanResult {
    public final List<Issue> issues;
    public final Metrics metrics;

    public ScanResult(List<Issue> issues, Metrics metrics) {
      this.issues = issues;
      this.metrics = metrics;
    }
  }
}
