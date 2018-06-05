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

import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.sonar.api.rules.RuleType;
import org.sonar.api.server.debt.DebtRemediationFunction;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.go.plugin.externalreport.ExternalKeyUtils;

import static org.assertj.core.api.Assertions.assertThat;

class GoRulesDefinitionTest {

  @Test
  void test() {
    GoRulesDefinition rulesDefinition = new GoRulesDefinition(false);
    RulesDefinition.Context context = new RulesDefinition.Context();
    rulesDefinition.define(context);

    assertThat(context.repositories()).hasSize(1);

    RulesDefinition.Repository goRepository = context.repository("go");

    assertThat(goRepository.name()).isEqualTo("SonarAnalyzer");
    assertThat(goRepository.language()).isEqualTo("go");
    assertThat(goRepository.rules()).hasSize(GoChecks.getChecks().size());

    assertRuleProperties(goRepository);
    assertAllRuleParametersHaveDescription(goRepository);
  }

  @Test
  public void test_external_repositories() {
    GoRulesDefinition rulesDefinition = new GoRulesDefinition(true);
    RulesDefinition.Context context = new RulesDefinition.Context();
    rulesDefinition.define(context);
    RulesDefinition.Repository golintRepository = context.repository("external_golint");
    RulesDefinition.Repository govetRepository = context.repository("external_govet");

    assertThat(context.repositories()).hasSize(3);

    assertThat(golintRepository.name()).isEqualTo("Golint");
    assertThat(govetRepository.name()).isEqualTo("go vet");

    assertThat(golintRepository.language()).isEqualTo("go");
    assertThat(govetRepository.language()).isEqualTo("go");

    assertThat(golintRepository.isExternal()).isEqualTo(true);
    assertThat(govetRepository.isExternal()).isEqualTo(true);

    assertThat(golintRepository.rules().size()).isEqualTo(18);
    assertThat(ExternalKeyUtils.GO_LINT_KEYS.size()).isEqualTo(18);

    assertThat(govetRepository.rules().size()).isEqualTo(21);
    assertThat(ExternalKeyUtils.GO_VET_KEYS.size()).isEqualTo(21);

    List<String> govetKeysWithoutDefinition = ExternalKeyUtils.GO_VET_KEYS.stream()
      .map(x -> x.key)
      .filter(key -> govetRepository.rule(key) == null)
      .collect(Collectors.toList());
    assertThat(govetKeysWithoutDefinition).isEmpty();

    List<String> golintKeysWithoutDefinition = ExternalKeyUtils.GO_LINT_KEYS.stream()
      .map(x -> x.key)
      .filter(key -> golintRepository.rule(key) == null)
      .collect(Collectors.toList());
    assertThat(golintKeysWithoutDefinition).isEmpty();
  }

  private void assertRuleProperties(RulesDefinition.Repository repository) {
    RulesDefinition.Rule rule = repository.rule("S2068");
    assertThat(rule).isNotNull();
    assertThat(rule.name()).isEqualTo("Credentials should not be hard-coded");
    assertThat(rule.debtRemediationFunction().type()).isEqualTo(DebtRemediationFunction.Type.CONSTANT_ISSUE);
    assertThat(rule.type()).isEqualTo(RuleType.VULNERABILITY);
  }

  private void assertAllRuleParametersHaveDescription(RulesDefinition.Repository repository) {
    for (RulesDefinition.Rule rule : repository.rules()) {
      for (RulesDefinition.Param param : rule.params()) {
        assertThat(param.description()).as("description for " + param.key()).isNotEmpty();
      }
    }
  }
}
