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
package org.sonar.commonruleengine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.commonruleengine.checks.Check;
import org.sonar.uast.UastNode;
import org.sonar.uast.Visitor;

public class EngineContext {

  private List<Issue> issues = new ArrayList<>();

  private Map<UastNode.Kind, List<Visitor>> registeredVisitors = new EnumMap<>(UastNode.Kind.class);
  private Set<Visitor> visitors = null;

  public EngineContext(Collection<Check> visitors) {
    this.visitors = new HashSet<>(visitors);
  }

  public void register(UastNode.Kind kind, Visitor visitor) {
    registeredVisitors.computeIfAbsent(kind, k -> new ArrayList<>()).add(visitor);
  }

  public List<Visitor> registeredVisitors(UastNode.Kind kind) {
    return registeredVisitors.getOrDefault(kind, Collections.emptyList());
  }

  public void reportIssue(Issue issue) {
    issues.add(issue);
  }

  void enterFile(InputFile inputFile) throws IOException {
    issues.clear();
    for (Visitor c : getVisitors()) {
      c.enterFile(inputFile);
    }
  }

  public List<Issue> getIssues() {
    return new ArrayList<>(issues);
  }

  private Set<Visitor> getVisitors() {
    return visitors;
  }
}
