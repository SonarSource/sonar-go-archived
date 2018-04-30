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
package org.sonar.go.plugin;

import java.util.Arrays;
import java.util.List;
import org.sonar.commonruleengine.checks.AllBranchesAreIdenticalCheck;
import org.sonar.commonruleengine.checks.BinaryOperatorIdenticalExpressionsCheck;
import org.sonar.commonruleengine.checks.CollapsibleIfCheck;
import org.sonar.commonruleengine.checks.CollectionSizeCheck;
import org.sonar.commonruleengine.checks.DoublePrefixOperatorCheck;
import org.sonar.commonruleengine.checks.EmptyBlockCheck;
import org.sonar.commonruleengine.checks.EmptyFunctionCheck;
import org.sonar.commonruleengine.checks.EmptyStatementsCheck;
import org.sonar.commonruleengine.checks.FileHeaderCheck;
import org.sonar.commonruleengine.checks.FixMeTagPresenceCheck;
import org.sonar.commonruleengine.checks.FunctionCognitiveComplexityCheck;
import org.sonar.commonruleengine.checks.FunctionNameConventionCheck;
import org.sonar.commonruleengine.checks.FunctionTooBigCheck;
import org.sonar.commonruleengine.checks.HardcodedIpAddressCheck;
import org.sonar.commonruleengine.checks.IfElseIfWithoutElseCheck;
import org.sonar.commonruleengine.checks.NestedSwitchCheck;
import org.sonar.commonruleengine.checks.NoHardcodedCredentialsCheck;
import org.sonar.commonruleengine.checks.NoIdenticalConditionsCheck;
import org.sonar.commonruleengine.checks.NoIdenticalFunctionsCheck;
import org.sonar.commonruleengine.checks.NoSelfAssignmentCheck;
import org.sonar.commonruleengine.checks.OctalNumberCheck;
import org.sonar.commonruleengine.checks.RedundantBooleanLiteralCheck;
import org.sonar.commonruleengine.checks.RedundantParenthesesCheck;
import org.sonar.commonruleengine.checks.StringLiteralDuplicatedCheck;
import org.sonar.commonruleengine.checks.SwitchCaseTooBigCheck;
import org.sonar.commonruleengine.checks.SwitchDefaultLocationCheck;
import org.sonar.commonruleengine.checks.SwitchWithTooManyCaseCheck;
import org.sonar.commonruleengine.checks.SwitchWithoutDefaultCheck;
import org.sonar.commonruleengine.checks.TodoTagPresenceCheck;
import org.sonar.commonruleengine.checks.TooLongLineCheck;
import org.sonar.commonruleengine.checks.TooManyLinesInFileCheck;
import org.sonar.commonruleengine.checks.TooManyParametersCheck;
import org.sonar.commonruleengine.checks.TwoBranchesAreIdenticalCheck;
import org.sonar.commonruleengine.checks.UnconditionalJumpStatementCheck;
import org.sonar.commonruleengine.checks.UnreachableCodeCheck;
import org.sonar.commonruleengine.checks.UselessIfCheck;
import org.sonar.commonruleengine.checks.WrongAssignmentOperatorCheck;

public class GoChecks {

  private GoChecks() {
    // do not instantiate
  }

  public static List<Class> getChecks() {
    return Arrays.asList(
      AllBranchesAreIdenticalCheck.class,
      BinaryOperatorIdenticalExpressionsCheck.class,
      CollapsibleIfCheck.class,
      CollectionSizeCheck.class,
      DoublePrefixOperatorCheck.class,
      FileHeaderCheck.class,
      EmptyBlockCheck.class,
      EmptyFunctionCheck.class,
      EmptyStatementsCheck.class,
      FixMeTagPresenceCheck.class,
      FunctionCognitiveComplexityCheck.class,
      FunctionTooBigCheck.class,
      HardcodedIpAddressCheck.class,
      IfElseIfWithoutElseCheck.class,
      FunctionNameConventionCheck.class,
      NestedSwitchCheck.class,
      NoIdenticalConditionsCheck.class,
      NoIdenticalFunctionsCheck.class,
      NoHardcodedCredentialsCheck.class,
      NoSelfAssignmentCheck.class,
      OctalNumberCheck.class,
      RedundantBooleanLiteralCheck.class,
      RedundantParenthesesCheck.class,
      StringLiteralDuplicatedCheck.class,
      SwitchCaseTooBigCheck.class,
      SwitchDefaultLocationCheck.class,
      SwitchWithoutDefaultCheck.class,
      SwitchWithTooManyCaseCheck.class,
      TodoTagPresenceCheck.class,
      TooLongLineCheck.class,
      TooManyLinesInFileCheck.class,
      TooManyParametersCheck.class,
      TwoBranchesAreIdenticalCheck.class,
      UnconditionalJumpStatementCheck.class,
      UnreachableCodeCheck.class,
      UselessIfCheck.class,
      WrongAssignmentOperatorCheck.class
    );
  }

}
