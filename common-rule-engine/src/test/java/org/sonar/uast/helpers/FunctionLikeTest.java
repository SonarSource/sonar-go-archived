package org.sonar.uast.helpers;

import java.io.StringReader;
import org.junit.jupiter.api.Test;
import org.sonar.uast.Uast;
import org.sonar.uast.UastNode;

import static org.assertj.core.api.Assertions.assertThat;

class FunctionLikeTest {

  @Test
  void test() throws Exception {
    UastNode node = Uast.from(new StringReader("" +
      "{\"kinds\": [\"FUNCTION\"], \"children\": [\n" +
      "  {\"kinds\": [\"KEYWORD\"], \"token\": {\"value\":\"func\",\"line\":4,\"column\":1}},\n" +
      "  {\"kinds\": [\"FUNCTION_NAME\",\"IDENTIFIER\"], \"token\": {\"value\":\"conv\",\"line\":4,\"column\":6}},\n" +
      "  {\"children\": [\n" +
      "    {\"kinds\": [\"PARAMETER_LIST\"], \"children\": [\n" +
      "      {\"token\": {\"value\":\"(\",\"line\":4,\"column\":10}},\n" +
      "      {\"children\": [\n" +
      "        {\"children\": [\n" +
      "          {\"kinds\": [\"PARAMETER\",\"IDENTIFIER\"], \"token\": {\"value\":\"a\",\"line\":4,\"column\":11}},\n" +
      "          {\"token\": {\"value\":\",\",\"line\":4,\"column\":12}},\n" +
      "          {\"kinds\": [\"PARAMETER\",\"IDENTIFIER\"], \"token\": {\"value\":\"b\",\"line\":4,\"column\":14}}\n" +
      "        ]},\n" +
      "        {\"kinds\": [\"TYPE\",\"IDENTIFIER\"], \"token\": {\"value\":\"int\",\"line\":4,\"column\":16}}\n" +
      "      ]},\n" +
      "      {\"token\": {\"value\":\")\",\"line\":4,\"column\":20}}\n" +
      "    ]},\n" +
      "    {\"kinds\": [\"RESULT_LIST\"], \"children\": [\n" +
      "      {\"children\": [\n" +
      "        {\"kinds\": [\"TYPE\",\"IDENTIFIER\"], \"token\": {\"value\":\"int\",\"line\":4,\"column\":22}}\n" +
      "      ]}\n" +
      "    ]}\n" +
      "  ]},\n" +
      "  {\"kinds\": [\"BLOCK\"], \"children\": [\n" +
      "    {\"token\": {\"value\":\"{\",\"line\":4,\"column\":26}},\n" +
      "    {\"token\": {\"value\":\"}\",\"line\":6,\"column\":1}}\n" +
      "  ]}\n" +
      "]}\n"));
    FunctionLike functionLike = FunctionLike.from(node);
    assertThat(functionLike).isNotNull();
    assertThat(functionLike.node().is(UastNode.Kind.FUNCTION)).isTrue();
    assertThat(functionLike.name().joinTokens()).isEqualTo("conv");
    assertThat(functionLike.parameters().size()).isEqualTo(2);
    assertThat(functionLike.body().is(UastNode.Kind.BLOCK)).isTrue();
  }

}
