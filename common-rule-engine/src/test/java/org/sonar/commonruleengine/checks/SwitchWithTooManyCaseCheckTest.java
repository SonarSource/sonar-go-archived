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
package org.sonar.commonruleengine.checks;

import org.junit.jupiter.api.Test;

import static org.sonar.commonruleengine.checks.TestUtils.checkRuleOnGo;
import static org.sonar.commonruleengine.checks.TestUtils.checkRuleOnJava;

public class SwitchWithTooManyCaseCheckTest {

  @Test
  void test_java() throws Exception {
    checkRuleOnJava(new SwitchWithTooManyCaseCheck());
  }

  @Test
  void test_java_set_max() throws Exception {
    SwitchWithTooManyCaseCheck check = new SwitchWithTooManyCaseCheck();
    check.maximumCases = 5;
    checkRuleOnJava(check, "SwitchWithTooManyCaseCheckMax5.java");
  }

  @Test
  void test_go() throws Exception {
    checkRuleOnGo(new SwitchWithTooManyCaseCheck());
  }

  @Test
  void test_go_set_max() throws Exception {
    SwitchWithTooManyCaseCheck check = new SwitchWithTooManyCaseCheck();
    check.maximumCases = 5;
    checkRuleOnGo(check, "SwitchWithTooManyCaseCheckMax5.go");
  }
}
