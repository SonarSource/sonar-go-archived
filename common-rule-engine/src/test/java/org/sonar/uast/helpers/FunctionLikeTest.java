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
package org.sonar.uast.helpers;

import java.io.StringReader;
import org.junit.jupiter.api.Test;
import org.sonar.uast.UastNode;

import static org.assertj.core.api.Assertions.assertThat;

class FunctionLikeTest {

  @Test
  void test() throws Exception {
    UastNode node = UastNode.from(new StringReader("" +
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
