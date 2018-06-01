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

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class GoVetKeys {

  private GoVetKeys() {
    // utility class, forbidden constructor
  }

  static final List<ExternalKey> GO_VET_KEYS = Arrays.asList(
          new ExternalKey("asmdecl", msg -> msg.startsWith("Invalid") && msg.contains("(FP\\)")),
          new ExternalKey("assign", msg -> msg.startsWith("self-assignment of")),
          new ExternalKey("atomic", msg -> msg.equals("direct assignment to atomic value")),
          new ExternalKey("bool", msg -> msg.startsWith("redundant") || msg.startsWith("suspect")),
          new ExternalKey("buildtag", msg -> msg.contains("build comment")),
          new ExternalKey("cgocall", msg -> msg.equals("possibly passing Go type with embedded pointer to C")),
          new ExternalKey("composites", msg -> msg.endsWith("composite literal uses unkeyed fields")),
          new ExternalKey("copylocks", msg -> msg.contains("passes lock by value:") || msg.contains("copies lock")),
          new ExternalKey("httpresponse", msg -> msg.endsWith("before checking for errors")),
          new ExternalKey("lostcancel", msg -> msg.matches("the cancel\\d? function .*") || msg.contains("without using the cancel")),
          new ExternalKey("methods", msg -> msg.contains("should have signature")),
          new ExternalKey("nilfunc", msg -> msg.contains("comparison of function")),
          new ExternalKey("printf", msg -> msg.matches("(Printf|Println|Sprintf|Sprintln|Logf|Log) .*") || msg.contains("formatting directive")),
          new ExternalKey("rangeloops", msg -> msg.startsWith("loop variable")),
          new ExternalKey("shadow", msg -> msg.contains("shadows declaration at")),
          new ExternalKey("shift", msg -> msg.contains("too small for shift")),
          new ExternalKey("structtags", msg -> msg.contains("struct field") && msg.contains("tag")),
          new ExternalKey("tests", msg ->
                          msg.contains("has malformed") ||
                          msg.contains("refers to unknown") ||
                          msg.endsWith("should return nothing") ||
                          msg.endsWith("should be niladic")),
          new ExternalKey("unreachable", msg -> msg.equals("unreachable code")),
          new ExternalKey("unusedresult", msg -> msg.endsWith("call not used")),
          new ExternalKey("unsafeptr", msg -> msg.equals("possible misuse of unsafe.Pointer"))
  );

  public static String lookup(String message) {
    return GO_VET_KEYS.stream()
            .filter(externalKey -> externalKey.matches.test(message))
            .map(externalKey -> externalKey.key)
            .findFirst()
            .orElse(AbstractReportSensor.GENERIC_ISSUE_KEY);
  }

  static class ExternalKey {
    String key;
    Predicate<String> matches;

    ExternalKey(String key, Predicate<String> matches) {
      this.key = key;
      this.matches = matches;
    }
  }
}




