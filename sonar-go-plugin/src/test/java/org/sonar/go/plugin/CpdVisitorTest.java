package org.sonar.go.plugin;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.fs.internal.DefaultTextPointer;
import org.sonar.api.batch.fs.internal.DefaultTextRange;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.cpd.NewCpdTokens;
import org.sonar.go.plugin.utils.AbstractInputFile;
import org.sonar.uast.Uast;
import org.sonar.uast.UastNode;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.assertj.core.api.Assertions.assertThat;

class CpdVisitorTest {

  @Test
  public void test() throws IOException {
    SensorContext context = mock(SensorContext.class);

    NewCpdTokensTester newCpdTokensTester = new NewCpdTokensTester();
    when(context.newCpdTokens()).thenReturn(newCpdTokensTester);

    CpdVisitor cpdVisitor = new CpdVisitor(context, new InputFileTester());

    String code = "{\"kinds\": [\"COMPILATION_UNIT\"], \"nativeNode\": \"(File)\", \"children\": [\n"
      + "  {\"kinds\": [\"PACKAGE\"], \"nativeNode\": \"File.Package\", \"children\": [\n"
      + "    {\"kinds\": [\"KEYWORD\"], \"token\": {\"value\":\"package\",\"line\":1,\"column\":1}, \"nativeNode\": \"\"},\n"
      + "    {\"kinds\": [\"IDENTIFIER\"], \"token\": {\"value\":\"main\",\"line\":1,\"column\":9}, \"nativeNode\": \"Name(Ident)\"}\n"
      + "  ]},\n"
      + "  {\"kinds\": [\"DECL_LIST\"], \"nativeNode\": \"Decls([]Decl)\", \"children\": [\n"
      + "    {\"kinds\": [\"FUNCTION\"], \"nativeNode\": \"[0](FuncDecl)\", \"children\": [\n"
      + "      {\"kinds\": [\"KEYWORD\"], \"token\": {\"value\":\"func\",\"line\":3,\"column\":1}, \"nativeNode\": \"Type.Func\"},\n"
      + "      {\"kinds\": [\"IDENTIFIER\"], \"token\": {\"value\":\"fun\",\"line\":3,\"column\":6}, \"nativeNode\": \"Name(Ident)\"},\n"
      + "      {\"nativeNode\": \"Type(FuncType)\", \"children\": [\n"
      + "        {\"kinds\": [\"PARAMETER_LIST\"], \"nativeNode\": \"Params(FieldList)\", \"children\": [\n"
      + "          {\"token\": {\"value\":\"(\",\"line\":3,\"column\":9}, \"nativeNode\": \"Opening\"},\n"
      + "          {\"token\": {\"value\":\")\",\"line\":3,\"column\":10}, \"nativeNode\": \"Closing\"}\n"
      + "        ]},\n"
      + "        {\"kinds\": [\"RESULT_LIST\"], \"nativeNode\": \"Results(FieldList)\", \"children\": [\n"
      + "          {\"nativeNode\": \"[0](Field)\", \"children\": [\n"
      + "            {\"kinds\": [\"TYPE\",\"IDENTIFIER\"], \"token\": {\"value\":\"string\",\"line\":3,\"column\":12}, \"nativeNode\": \"Type(Ident)\"}\n"
      + "          ]}\n"
      + "        ]}\n"
      + "      ]},\n"
      + "      {\"kinds\": [\"BLOCK\"], \"nativeNode\": \"Body(BlockStmt)\", \"children\": [\n"
      + "        {\"token\": {\"value\":\"{\",\"line\":3,\"column\":19}, \"nativeNode\": \"Lbrace\"},\n"
      + "        {\"kinds\": [\"ASSIGNMENT\",\"STATEMENT\"], \"nativeNode\": \"[0](AssignStmt)\", \"children\": [\n"
      + "          {\"kinds\": [\"ASSIGNMENT_TARGET\"], \"nativeNode\": \"Lhs([]Expr)\", \"children\": [\n"
      + "            {\"kinds\": [\"IDENTIFIER\"], \"token\": {\"value\":\"a\",\"line\":4,\"column\":2}, \"nativeNode\": \"[0](Ident)\"}\n"
      + "          ]},\n"
      + "          {\"token\": {\"value\":\":=\",\"line\":4,\"column\":4}, \"nativeNode\": \"Tok\"},\n"
      + "          {\"kinds\": [\"ASSIGNMENT_VALUE\"], \"nativeNode\": \"Rhs([]Expr)\", \"children\": [\n"
      + "            {\"kinds\": [\"LITERAL\",\"STRING_LITERAL\"], \"token\": {\"value\":\"\\\"hello \\\\\\\"world\\\\\\\"\\\"\",\"line\":4,\"column\":7}, \"nativeNode\": \"[0](BasicLit)\"}\n"
      + "          ]}\n"
      + "        ]},\n"
      + "        {\"kinds\": [\"STATEMENT\"], \"nativeNode\": \"[1](ReturnStmt)\", \"children\": [\n"
      + "          {\"kinds\": [\"KEYWORD\"], \"token\": {\"value\":\"return\",\"line\":5,\"column\":2}, \"nativeNode\": \"Return\"},\n"
      + "          {\"kinds\": [\"IDENTIFIER\"], \"token\": {\"value\":\"a\",\"line\":5,\"column\":9}, \"nativeNode\": \"[0](Ident)\"}\n"
      + "        ]},\n"
      + "        {\"token\": {\"value\":\"}\",\"line\":6,\"column\":1}, \"nativeNode\": \"Rbrace\"}\n"
      + "      ]}\n"
      + "    ]}\n"
      + "  ]},\n"
      + "  {\"kinds\": [\"EOF\"], \"token\": {\"line\":7,\"column\":1}, \"nativeNode\": \"\"}\n"
      + "]}";
    UastNode node = Uast.from(new StringReader(code));
    cpdVisitor.scan(node);

    assertThat(newCpdTokensTester.values.size()).isEqualTo(14);
    assertThat(newCpdTokensTester.values).isEqualTo(Arrays.asList(
      "package", "main",
      "func", "fun", "(", ")", "string", "{",
      "a", ":=", "LITERAL",
      "return", "a",
      "}"));
    List<Integer> lines = Arrays.asList(
      1, 1,
      3, 3, 3, 3, 3, 3,
      4, 4, 4,
      5, 5,
      6);
    assertThat(newCpdTokensTester.ranges).extracting("start.line").isEqualTo(lines);
    assertThat(newCpdTokensTester.ranges).extracting("end.line").isEqualTo(lines);
  }

  static class AbstractNewCpdTokens implements NewCpdTokens {
    @Override
    public NewCpdTokens onFile(InputFile inputFile) {
      return this;
    }

    @Override
    public NewCpdTokens addToken(TextRange range, String image) {
      return this;
    }

    @Override
    public NewCpdTokens addToken(int startLine, int startLineOffset, int endLine, int endLineOffset, String image) {
      return this;
    }

    @Override
    public void save() {

    }
  }

  static class NewCpdTokensTester extends AbstractNewCpdTokens {
    List<TextRange> ranges = new ArrayList<>();
    List<String> values = new ArrayList<>();

    @Override
    public NewCpdTokens addToken(TextRange range, String image) {
      ranges.add(range);
      values.add(image);
      return this;
    }
  }

  static class InputFileTester extends AbstractInputFile {
    @Override
    public TextRange newRange(int startLine, int startLineOffset, int endLine, int endLineOffset) {
      return new DefaultTextRange(new DefaultTextPointer(startLine, startLineOffset), new DefaultTextPointer(endLine, endLineOffset));
    }
  }

}
