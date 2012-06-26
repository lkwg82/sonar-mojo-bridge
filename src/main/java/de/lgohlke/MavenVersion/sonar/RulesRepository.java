package de.lgohlke.MavenVersion.sonar;

import org.sonar.api.rules.AnnotationRuleParser;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleRepository;

import java.util.Arrays;
import java.util.List;

public class RulesRepository extends RuleRepository {

  private final AnnotationRuleParser ruleParser;

  public RulesRepository(final AnnotationRuleParser ruleParser) {
    super(MavenPlugin.REPOSITORY_KEY, "java");
    setName(MavenPlugin.REPOSITORY_NAME);
    this.ruleParser = ruleParser;
  }

  @Override
  public List<Rule> createRules() {
    return ruleParser.parse(MavenPlugin.REPOSITORY_KEY, getCheckedClasses());
  }

  @SuppressWarnings("rawtypes")
  private static List<Class> getCheckedClasses() {
    return Arrays.asList((Class) DependencyVersionMavenRule.class, PluginVersionMavenRule.class);
  }

}
