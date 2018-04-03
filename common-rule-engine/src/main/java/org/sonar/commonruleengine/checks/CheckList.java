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

import java.util.Arrays;
import java.util.List;

public class CheckList {

  private CheckList() {
    // do not instantiate
  }

  public static List<Class<? extends Check>> getChecks() {
    return Arrays.asList(
      AllBranchesAreIdenticalCheck.class,
      BinaryOperatorIdenticalExpressionsCheck.class,
      FileHeaderCheck.class,
      FunctionCognitiveComplexityCheck.class,
      NestedSwitchCheck.class,
      NoIdenticalConditionsCheck.class,
      NoIdenticalFunctionsCheck.class,
      NoHardcodedCredentialsCheck.class,
      NoSelfAssignmentCheck.class,
      RedundantBooleanLiteralCheck.class,
      RedundantParenthesesCheck.class,
      SwitchDefaultLocationCheck.class,
      SwitchWithoutDefaultCheck.class,
      TooManyParametersCheck.class,
      WrongAssignmentOperatorCheck.class
    );
  }

}
