package org.sonar.go.plugin;

import org.junit.jupiter.api.Test;
import org.sonar.api.rules.RuleType;
import org.sonar.api.server.debt.DebtRemediationFunction;
import org.sonar.api.server.rule.RulesDefinition;

import static org.assertj.core.api.Assertions.assertThat;

class GoRulesDefinitionTest {

  @Test
  void test() {
    RulesDefinition.Repository repository = buildRepository();

    assertThat(repository.name()).isEqualTo("SonarAnalyzer");
    assertThat(repository.language()).isEqualTo("go");
    assertThat(repository.rules()).hasSize(GoChecks.getChecks().size());

    assertRuleProperties(repository);
    assertAllRuleParametersHaveDescription(repository);
  }

  private RulesDefinition.Repository buildRepository() {
    GoRulesDefinition rulesDefinition = new GoRulesDefinition();
    RulesDefinition.Context context = new RulesDefinition.Context();
    rulesDefinition.define(context);
    return context.repository("Go");
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
