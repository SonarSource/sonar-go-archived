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
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class ExternalKeyUtils {

  private ExternalKeyUtils() {
    // utility class, forbidden constructor
  }

  public static final List<ExternalKey> GO_VET_KEYS = Collections.unmodifiableList(Arrays.asList(
    new ExternalKey("asmdecl", msg -> msg.startsWith("Invalid") && msg.contains("(FP\\)")),
    new ExternalKey("assign", msg -> msg.startsWith("self-assignment of")),
    new ExternalKey("atomic", msg -> msg.equals("direct assignment to atomic value")),
    new ExternalKey("bool", msg -> msg.startsWith("redundant") || msg.startsWith("suspect")),
    new ExternalKey("buildtags", msg -> msg.contains("build comment")),
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
  ));


  public static final List<ExternalKey> GO_LINT_KEYS = Collections.unmodifiableList(Arrays.asList(
    new ExternalKey("PackageComment", msg ->
      msg.startsWith("package comment should be of the form") ||
        msg.startsWith("package comment should not have leading space") ||
        msg.equals("package comment is detached; there should be no blank lines between it and the package statement") ||
        msg.equals("should have a package comment, unless it's in another file for this package")),
    new ExternalKey("BlankImports", msg -> msg.equals("a blank import should be only in a main or test package, or have a comment justifying it")),
    new ExternalKey("Imports", msg -> msg.equals("should not use dot imports")),
    new ExternalKey("Exported", msg -> (msg.startsWith("exported") && msg.endsWith("or be unexported")) ||
      msg.startsWith("comment on exported") ||
      msg.endsWith("should have its own declaration") ||
      msg.contains("by other packages, and that stutters; consider calling this")),
    new ExternalKey("VarDecls", msg -> msg.contains("from declaration of var")),
    new ExternalKey("Elses", msg -> msg.startsWith("if block ends with a return statement, so drop this else and outdent its block")),
    new ExternalKey("Ranges", msg -> msg.contains("from range; this loop is equivalent to")),
    new ExternalKey("Errorf", msg -> msg.contains("(fmt.Sprintf(...)) with") && msg.contains(".Errorf(...)")),
    new ExternalKey("Errors", msg -> msg.startsWith("error var ") && msg.contains("should have name of the form ")),
    new ExternalKey("ErrorStrings", msg -> msg.equals("error strings should not be capitalized or end with punctuation or a newline")),
    new ExternalKey("ReceiverNames", msg ->
      msg.contains("should be consistent with previous receiver name") ||
        msg.startsWith("receiver name should not be an underscore") ||
        msg.equals("receiver name should be a reflection of its identity; don't use generic names such as \"this\" or \"self\"")),
    new ExternalKey("IncDec", msg -> msg.startsWith("should replace") && !msg.contains("(fmt.Sprintf(...)) with")),
    new ExternalKey("ErrorReturn", msg -> msg.startsWith("error should be the last type when returning multiple items")),
    new ExternalKey("UnexportedReturn", msg -> msg.contains("returns unexported type") && msg.endsWith("which can be annoying to use")),
    new ExternalKey("TimeNames", msg -> msg.contains("don't use unit-specific suffix")),
    new ExternalKey("ContextKeyTypes", msg -> msg.startsWith("should not use basic type") && msg.endsWith("as key in context.WithValue")),
    new ExternalKey("ContextArgs", msg -> msg.equals("context.Context should be the first parameter of a function")),
    new ExternalKey("Names", msg ->
      msg.startsWith("don't use an underscore in package name") ||
        msg.startsWith("don't use ALL_CAPS in Go names; use CamelCase") ||
        msg.startsWith("don't use leading k in Go names;") ||
        msg.startsWith("don't use underscores in Go names;") ||
        msg.matches("(range var|struct field|[\\w]+) [\\w_]+ should be [\\w_]+") ||
        msg.startsWith("don't use MixedCaps in package name;")
    )
  ));

  public static String lookup(String message, String linter) {
    if (linter.equals(GoVetReportSensor.LINTER_ID) || linter.equals(GoLintReportSensor.LINTER_ID)) {
      List<ExternalKey> keys = linter.equals(GoVetReportSensor.LINTER_ID) ? GO_VET_KEYS : GO_LINT_KEYS;
      return keys.stream()
        .filter(externalKey -> externalKey.matches.test(message))
        .map(externalKey -> externalKey.key)
        .findFirst()
        .orElse(null);
    }
    return null;
  }

  public static class ExternalKey {
    public final String key;
    public final Predicate<String> matches;

    ExternalKey(String key, Predicate<String> matches) {
      this.key = key;
      this.matches = matches;
    }
  }
}
