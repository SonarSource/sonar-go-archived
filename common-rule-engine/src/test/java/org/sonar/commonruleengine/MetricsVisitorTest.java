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
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.uast.UastNode;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MetricsVisitorTest {

  @Test
  void number_of_classes() throws Exception {
    Metrics metrics = getMetrics("{children:[" +
      "  {kinds:[ 'CLASS' ]}," +
      "  {kinds:[ 'CLASS' ]}" +
      "]}");
    assertEquals(2, metrics.numberOfClasses);
  }

  @Test
  void number_of_functions() throws Exception {
    Metrics metrics = getMetrics(
      "{ kinds: [\"COMPILATION_UNIT\"],\n" +
        "  children: [\n" +
        "    { children: [\n" +
        "        { kinds: [\"FUNCTION\"] },\n" +
        "        { kinds: [\"FUNCTION\"] },\n" +
        "        { kinds: [\"FUNCTION\"] }\n" +
        "      ]\n" +
        "    }\n" +
        "  ]\n" +
        "}\n");
    assertEquals(3, metrics.numberOfFunctions);
  }

  @Test
  void number_of_statements() throws Exception {
    Metrics metrics = getMetrics("{children:[" +
      "  {kinds:[ 'STATEMENT' ]}," +
      "  {kinds:[ 'STATEMENT' ]}," +
      "  {kinds:[ 'STATEMENT' ]}," +
      "  {kinds:[ 'STATEMENT' ]}" +
      "]}");
    assertEquals(4, metrics.numberOfStatements);
  }

  @Test
  void node_with_several_kinds() throws Exception {
    Metrics metrics = getMetrics("{children:[" +
      "  {kinds:[ 'STATEMENT', 'FUNCTION' ]}," +
      "  {kinds:[ 'STATEMENT', 'CLASS' ]}" +
      "]}");
    assertEquals(2, metrics.numberOfStatements);
    assertEquals(1, metrics.numberOfFunctions);
    assertEquals(1, metrics.numberOfClasses);
  }

  @Test
  void number_of_lines_of_code_and_comments() throws Exception {
    Metrics metrics = getMetrics("{children:[" +
      "  {kinds:[ 'STATEMENT' ], token: {line: 1, column: 2, value: 'a'}}," +
      "  {kinds:[ 'STATEMENT' ], token: {line: 2, column: 2, value: 'b'}}," +
      "  {kinds:[ 'COMMENT'   ], token: {line: 2, column: 4, value: '// Single line' }}," +
      "  {kinds:[ 'COMMENT'   ], token: {line: 4, column: 2, value: '/* multi \\n line */'}}" +
      "]}");
    assertEquals(new HashSet<>(Arrays.asList(1, 2)), metrics.linesOfCode);
    assertEquals(new HashSet<>(Arrays.asList(2, 4, 5)), metrics.commentLines);
  }

  @Test
  void executable_lines() throws Exception {
    Metrics metrics = getMetrics("{children:[" +
      "  {kinds:[ 'STATEMENT' ],  token: {line: 1, column: 2, value: 'a'}}," +
      "  {kinds:[ 'COMMENT'   ],  token: {line: 2, column: 2, value: '// Single line' }}," +
      "  {kinds:[ 'STATEMENT' ],  token: {line: 3, column: 2, value: 'b'}}," +
      "  {kinds:[ 'EXPRESSION' ], token: {line: 4, column: 2, value: 'x'}}," +
      "  {kinds:['SWITCH'], " +
      "children:[ " +
      "   {kinds:['KEYWORD'], token:{line: 5, column: 2, value: 'switch'}}, " +
      "   {kinds:['EXPRESSION']}, " +
      "   {kinds:[ 'CASE' ], children:[{kinds:['KEYWORD'], token : {line: 5, column: 2, value: 'case'}}, {kinds:['CONDITION']}]}" +
      "]}," +
      "  {kinds:[ 'LABEL' ],      token: {line: 6, column: 2, value: 'y:'}}" +
      "]}");
    assertEquals(new HashSet<>(Arrays.asList(1, 3, 4, 5, 6)), metrics.executableLines);
  }

  private Metrics getMetrics(String source) throws IOException {
    Engine engine = new Engine(Collections.emptyList());
    UastNode node = UastNode.from(new StringReader(source));
    InputFile inputFile = TestInputFileBuilder.create(".", "foo.go").setType(InputFile.Type.MAIN).build();
    return engine.scan(node, inputFile).metrics;
  }

}
