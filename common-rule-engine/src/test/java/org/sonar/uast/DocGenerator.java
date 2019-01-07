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
package org.sonar.uast;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.sonar.uast.UastNode.Kind;
import org.sonar.uast.generator.java.Generator;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.sonar.commonruleengine.checks.TestUtils.createGoParserKinds;

public class DocGenerator {

  private static final Path DOC_PATH = Paths.get("docs", "UAST-Kinds.md");

  private static final Set<Kind> EXPLICIT_ROOT_KIND = new HashSet<>(Arrays.asList(
    Kind.BLOCK,
    Kind.EXPRESSION
  ));

  private static final Map<Kind, List<Kind>> KIND_COMPOSITION = new HashMap<>();
  static {
    KIND_COMPOSITION.put(Kind.COMPILATION_UNIT, Collections.singletonList(
      Kind.EOF));
    KIND_COMPOSITION.put(Kind.FUNCTION, Arrays.asList(
      Kind.FUNCTION_NAME,
      Kind.RESULT_LIST,
      Kind.PARAMETER_LIST,
      Kind.PARAMETER,
      Kind.BODY));
    KIND_COMPOSITION.put(Kind.SWITCH, Arrays.asList(
      Kind.CASE,
      Kind.CONDITION,
      Kind.DEFAULT_CASE,
      Kind.BLOCK));
    KIND_COMPOSITION.put(Kind.IF, Arrays.asList(
      Kind.IF_KEYWORD,
      Kind.CONDITION,
      Kind.THEN,
      Kind.ELSE_KEYWORD,
      Kind.ELSE));
    KIND_COMPOSITION.put(Kind.CONDITIONAL_EXPRESSION, Arrays.asList(
      Kind.CONDITION,
      Kind.THEN,
      Kind.ELSE));
    KIND_COMPOSITION.put(Kind.FOR, Arrays.asList(
      Kind.FOR_KEYWORD,
      Kind.FOR_INIT,
      Kind.CONDITION,
      Kind.FOR_UPDATE,
      Kind.BODY));
    KIND_COMPOSITION.put(Kind.WHILE, Arrays.asList(
      Kind.CONDITION,
      Kind.BODY));
    KIND_COMPOSITION.put(Kind.ARRAY_ACCESS_EXPRESSION, Arrays.asList(
      Kind.ARRAY_OBJECT_EXPRESSION,
      Kind.ARRAY_KEY_EXPRESSION));
    KIND_COMPOSITION.put(Kind.PARENTHESIZED_EXPRESSION, Arrays.asList(
      Kind.LEFT_PARENTHESIS,
      Kind.EXPRESSION,
      Kind.RIGHT_PARENTHESIS));
    KIND_COMPOSITION.put(Kind.ASSIGNMENT, Arrays.asList(
      Kind.ASSIGNMENT_TARGET_LIST,
      Kind.ASSIGNMENT_TARGET,
      Kind.ASSIGNMENT_OPERATOR,
      Kind.ASSIGNMENT_VALUE_LIST,
      Kind.ASSIGNMENT_VALUE));
    KIND_COMPOSITION.put(Kind.BREAK, Collections.singletonList(
      Kind.BRANCH_LABEL));
    KIND_COMPOSITION.put(Kind.CONTINUE, Collections.singletonList(
      Kind.BRANCH_LABEL));
    KIND_COMPOSITION.put(Kind.GOTO, Collections.singletonList(
      Kind.BRANCH_LABEL));
    KIND_COMPOSITION.put(Kind.CALL, Arrays.asList(
      Kind.ARGUMENTS,
      Kind.ARGUMENT));
    KIND_COMPOSITION.put(Kind.VARIABLE_DECLARATION, Arrays.asList(
      Kind.VARIABLE_NAME,
      Kind.TYPE));
    KIND_COMPOSITION.put(Kind.IMPORT, Collections.singletonList(
      Kind.IMPORT_ENTRY));
    KIND_COMPOSITION.put(Kind.TYPE_PARAMETERS, Collections.singletonList(
      Kind.TYPE_PARAMETER));
    KIND_COMPOSITION.put(Kind.TYPE_ARGUMENTS, Collections.singletonList(
      Kind.TYPE_ARGUMENT));
  }

  public static void main(String[] args) throws IOException {
    Files.write(DOC_PATH, generate().getBytes(UTF_8));
  }

  private static String generate() {

    Map<String, List<String>> kindByLanguage = new HashMap<>();
    javaParserKinds()
      .forEach(kindName -> kindByLanguage.computeIfAbsent(kindName, name -> new ArrayList<>()).add("Java"));
    goParserKinds()
      .forEach(kindName -> kindByLanguage.computeIfAbsent(kindName, name -> new ArrayList<>()).add("Go"));


    StringBuilder out = new StringBuilder();
    Map<Kind, DocumentedKind> hierarchyMap = Arrays.stream(Kind.values())
      .map(DocumentedKind::new)
      .collect(Collectors.toMap(k -> k.kind, Function.identity()));
    hierarchyMap.values().forEach(hierarchy -> hierarchy.updateReferences(hierarchyMap, kindByLanguage));

    out.append("# UAST Kinds\n");
    out.append("_(do not edit, this page is generated)_\n");
    out.append("\n");
    out.append("**Summary**\n");
    out.append("* [Kinds Hierarchy](#kinds-hierarchy)\n");
    out.append("* [Kinds Properties](#kinds-properties)\n");
    out.append("\n");
    out.append("## Kinds Hierarchy\n");
    out.append("\n");

    hierarchyMap.values().stream()
      .filter(DocumentedKind::isRoot)
      .sorted(DocumentedKind.COMPARATOR)
      .forEach(hierarchy -> hierarchy.printHierarchy(out, 0));

    out.append("\n");
    out.append("## Kinds Properties\n");
    out.append("\n");

    hierarchyMap.values().stream()
      .sorted(DocumentedKind.COMPARATOR)
      .forEach(hierarchy -> hierarchy.printDefinition(out));

    return out.toString();
  }

  static class DocumentedKind {

    static final Comparator<DocumentedKind> COMPARATOR = Comparator.comparing(k -> k.name);

    final boolean isRoot;
    final String name;
    final Kind kind;
    final Set<Kind> derivedKinds;
    List<String> languages = Collections.emptyList();
    Set<DocumentedKind> extended = Collections.emptySet();
    List<DocumentedKind> components = Collections.emptyList();
    Set<DocumentedKind> derived = Collections.emptySet();
    Set<DocumentedKind> directlyDerived = Collections.emptySet();

    DocumentedKind(Kind kind) {
      this.kind = kind;
      name = toPascalCase(kind.name());
      derivedKinds = Arrays.stream(Kind.values())
        .filter(k -> k.extendedKinds().contains(this.kind))
        .collect(Collectors.toSet());
      boolean component = KIND_COMPOSITION.values().stream()
        .flatMap(List::stream)
        .anyMatch(kind::equals);
      isRoot = EXPLICIT_ROOT_KIND.contains(kind) || (kind.extendedKinds().isEmpty() && !component);
    }

    boolean isRoot() {
      return isRoot;
    }

    String link() {
      return "[" + name + "](#" + name.toLowerCase(Locale.ROOT).replace(' ', '-') + ")";
    }

    void updateReferences(Map<Kind, DocumentedKind> hierarchyMap, Map<String, List<String>> kindByLanguage) {
      languages = kindByLanguage.getOrDefault(kind.name(), Collections.emptyList());
      extended = kind.extendedKinds().stream()
        .map(hierarchyMap::get)
        .collect(Collectors.toSet());
      components = Optional.ofNullable(KIND_COMPOSITION.get(kind))
        .map(list -> list.stream().map(hierarchyMap::get).collect(Collectors.toList()))
        .orElse(Collections.emptyList());
      derived = derivedKinds.stream()
        .map(hierarchyMap::get)
        .collect(Collectors.toSet());
      directlyDerived = derived.stream()
        .filter(docKind -> derivedKinds.stream().filter(kind -> kind != docKind.kind)
          .noneMatch(kind -> hierarchyMap.get(kind).derivedKinds.contains(docKind.kind)))
        .collect(Collectors.toSet());
    }

    void printHierarchy(StringBuilder out, int indent) {
      for (int i = 0; i < indent; i++) {
        out.append("    ");
      }
      out.append("* ").append(link());
      if (!components.isEmpty()) {
        out.append(" { ").append(links(components)).append(" }");
      }
      out.append("\n");
      directlyDerived.stream()
        .sorted(DocumentedKind.COMPARATOR)
        .forEach(hierarchy -> hierarchy.printHierarchy(out, indent + 1));
    }

    void printDefinition(StringBuilder out) {
      out.append("### ").append(name).append("\n");
      out.append(    "Key | ").append(kind.name()).append("\n");
      out.append(    "--- | ---").append("\n");
      if (!extended.isEmpty()) {
        out.append(  "Extends | ").append(sortedLinks(extended)).append("\n");
      }
      if (!directlyDerived.isEmpty()) {
        out.append(  "Direct sub-kinds | ").append(sortedLinks(directlyDerived)).append("\n");
        if (derived.size() != directlyDerived.size()) {
          out.append("All sub-kinds | ").append(sortedLinks(derived)).append("\n");
        }
      }
      if (!components.isEmpty()) {
        out.append("Components | ").append(links(components)).append("\n");
      }
      out.append(  "Languages | ").append(languages.stream().collect(Collectors.joining(", "))).append("\n");
      out.append("\n").append("\n");
    }

  }

  private static String links(Collection<DocumentedKind> list) {
    return list.stream()
      .map(DocumentedKind::link)
      .collect(Collectors.joining(", "));
  }

  private static String sortedLinks(Collection<DocumentedKind> list) {
    return list.stream()
      .sorted(DocumentedKind.COMPARATOR)
      .map(DocumentedKind::link)
      .collect(Collectors.joining(", "));
  }

  private static String toPascalCase(String original) {
    StringBuilder result = new StringBuilder();
    for (String part : original.split("[ _]+")) {
      if (!part.isEmpty()) {
        result.append(Character.toUpperCase(part.charAt(0)));
        result.append(part.substring(1).toLowerCase(Locale.ROOT));
      }
    }
    return result.toString();
  }

  private static Set<String> javaParserKinds() {
    return Generator.allKindNames();
  }

  private static Set<String>  goParserKinds() {
    try {
      return Arrays.stream(new String(Files.readAllBytes(createGoParserKinds()), UTF_8)
        .split("\r?\n"))
        .filter(line -> !line.isEmpty())
        .collect(Collectors.toSet());
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

}
