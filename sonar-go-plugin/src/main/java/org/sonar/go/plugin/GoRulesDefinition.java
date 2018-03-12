package org.sonar.go.plugin;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonarsource.analyzer.commons.RuleMetadataLoader;

public class GoRulesDefinition implements RulesDefinition {

  public static final String REPOSITORY_KEY = "Go";

  @Override
  public void define(Context context) {
    NewRepository repository = context.createRepository(REPOSITORY_KEY, GoLanguage.KEY)
      .setName("SonarAnalyzer");
    RuleMetadataLoader metadataLoader = new RuleMetadataLoader(GoPlugin.RESOURCE_FOLDER);
    metadataLoader.addRulesByAnnotatedClass(repository, GoChecks.getChecks());
    repository.done();
  }
}
