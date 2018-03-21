package org.sonar.commonruleengine;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import org.junit.jupiter.api.Test;
import org.sonar.uast.Uast;
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
        "    { kinds: [\"DECL_LIST\"],\n" +
        "      children: [\n" +
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
      "  {kinds:[ 'CASE' ],       token: {line: 5, column: 2, value: 'case x:'}}," +
      "  {kinds:[ 'LABEL' ],      token: {line: 6, column: 2, value: 'y:'}}" +
      "]}");
    assertEquals(new HashSet<>(Arrays.asList(1, 3, 4, 5, 6)), metrics.executableLines);
  }

  private Metrics getMetrics(String source) throws IOException {
    Engine engine = new Engine(Collections.emptyList());
    UastNode node = Uast.from(new StringReader(source));
    return engine.scan(node).metrics;
  }

}
