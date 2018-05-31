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

import org.sonar.api.rules.RuleType;

import javax.annotation.Nullable;

class ExternalIssue {

  final String linter;
  final RuleType type;
  @Nullable
  final String ruleKey;
  final String filename;
  final int lineNumber;
  final String message;

  ExternalIssue(String linter, RuleType type, @Nullable String ruleKey, String filename, int lineNumber, String message) {
    this.linter = linter;
    this.type = type;
    this.ruleKey = ruleKey;
    this.filename = filename;
    this.lineNumber = lineNumber;
    this.message = message;
  }

}
