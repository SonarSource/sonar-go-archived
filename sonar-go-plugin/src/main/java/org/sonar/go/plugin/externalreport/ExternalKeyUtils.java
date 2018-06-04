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

public class ExternalKeyUtils {

  private ExternalKeyUtils() {
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

  static final List<ExternalKey> GO_LINT_KEYS = Arrays.asList(
          new ExternalKey("AllCaps", msg -> msg.startsWith("don't use ALL_CAPS in Go names; use CamelCase")),
          new ExternalKey("AnnoyingUseExportedType", msg -> msg.contains("returns unexported type") && msg.endsWith("which can be annoying to use")),
          new ExternalKey("BlankImport", msg -> msg.equals("a blank import should be only in a main or test package, or have a comment justifying it")),
          new ExternalKey("CommentExportedType", msg ->
                  msg.matches("comment on exported type [a-zA-Z0-9]+ should be of the form \"[a-zA-Z0-9]+ ...\" \\\\(with optional leading article\\\\)")),
          new ExternalKey("ContextContextFirstArg", msg -> msg.equals("context.Context should be the first parameter of a function")),
          new ExternalKey("DropAssignment", msg -> msg.contains("from declaration of var") && msg.endsWith("it is the zero value")),
          new ExternalKey("ErrorEndString", msg -> msg.startsWith("error strings should not be capitalized or end with punctuation or a newline")),
          new ExternalKey("ErrorLastReturn", msg -> msg.startsWith("error should be the last type when returning multiple items")),
          new ExternalKey("ErrorVarName", msg -> msg.matches("error var [a-zA-Z0-9]+ should have name of the form [a-zA-Z0-9]+")),
          new ExternalKey("ExportedDeclaration", msg -> msg.endsWith("should have its own declaration")),
          new ExternalKey("ExportedHaveComment", msg -> msg.matches("exported [a-zA-Z0-9\\.]+ [a-zA-Z0-9\\.]+ should have comment[\\(\\) a-zA-Z0-9]* or be unexported")),
          new ExternalKey("FormComment", msg -> msg.startsWith("comment on exported")),
          new ExternalKey("HavePackageComment", msg -> msg.equals("should have a package comment, unless it's in another file for this package")),
          new ExternalKey("IfBlockReturn", msg -> msg.contains("if block ends with a return statement, so drop this else and outdent its block")),
          new ExternalKey("Initialisms", msg -> msg.matches("(range var|[a-zA-Z0-9]+) [a-zA-Z0-9]+ should be [a-zA-Z0-9]+")),
          new ExternalKey("LeadingK", msg -> msg.startsWith("don't use leading k in Go names;")),
          new ExternalKey("NotUseBasicType", msg -> msg.startsWith("should not use basic type")),
          new ExternalKey("NotUseDotImport", msg -> msg.equals("should not use dot imports")),
          new ExternalKey("OmitType", msg -> msg.startsWith("should omit type") && msg.endsWith("it will be inferred from the right-hand side")),
          new ExternalKey("PackageCommentForm", msg -> msg.startsWith("package comment should be of the form")),
          new ExternalKey("PackageCommentSpace", msg -> msg.startsWith("package comment should not have leading space")),
          new ExternalKey("PackageCommentDetached", msg -> msg.equals("package comment is detached; there should be no blank lines between it and the package statement")),
          new ExternalKey("PackageNames", msg ->
                  msg.matches("[a-zA-Z0-9]+ name will be used as [a-zA-Z0-9]+.[a-zA-Z0-9]+ by other packages, and that stutters; consider calling this [a-zA-Z0-9]+")),
          new ExternalKey("PackageNamesMixedCaps", msg -> msg.startsWith("don't use MixedCaps in package name;")),
          new ExternalKey("ReceiverNameConsistent", msg -> msg.contains("should be consistent with previous receiver name")),
          new ExternalKey("ReceiverNameNotUnderscore", msg -> msg.startsWith("receiver name should not be an underscore")),
          new ExternalKey("ReceiverNameReflection", msg ->
                  msg.equals("receiver name should be a reflection of its identity; don't use generic names such as \"this\" or \"self\"")),
          new ExternalKey("ReplaceLintIncDec", msg -> msg.matches("should replace [a-zA-Z0-9\\-\\+=\\.\\(\\) ]+ with [a-zA-Z0-9\\-\\+\\.\\(\\)]+")),
          new ExternalKey("ReplaceSprintf", msg -> msg.matches("should replace [a-zA-Z0-9\\.]+\\\\(fmt.Sprintf\\\\(...\\\\)\\\\) with [a-zA-Z0-9]+.Errorf\\\\(...\\\\)")),
          new ExternalKey("RangeLoop", msg -> msg.matches("should omit (2nd value|values) from range; this loop is equivalent to .*")),
          new ExternalKey("SpecificSuffix", msg -> msg.contains("don't use unit-specific suffix")),
          new ExternalKey("StructFieldInitialisms", msg -> msg.matches("struct field [a-zA-Z0-9]+ should be [a-zA-Z0-9]+")),
          new ExternalKey("UnderscoreInGoName", msg -> msg.startsWith("don't use underscores in Go names;")),
          new ExternalKey("UnderscoreInPackageName", msg -> msg.startsWith("don't use an underscore in package name"))
  );

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

  static class ExternalKey {
    String key;
    Predicate<String> matches;

    ExternalKey(String key, Predicate<String> matches) {
      this.key = key;
      this.matches = matches;
    }
  }
}
