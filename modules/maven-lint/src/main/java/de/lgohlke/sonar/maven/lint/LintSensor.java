/*
 * sonar-mojo-bridge-maven-lint
 * Copyright (C) 2012 Lars Gohlke
 * dev@sonar.codehaus.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package de.lgohlke.sonar.maven.lint;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import de.lgohlke.sonar.Configuration;
import de.lgohlke.sonar.maven.MavenPluginHandlerFactory;
import de.lgohlke.sonar.maven.MavenRule;
import de.lgohlke.sonar.maven.RuleUtils;
import de.lgohlke.sonar.maven.Rules;
import de.lgohlke.sonar.maven.lint.rules.*;
import de.lgohlke.sonar.maven.lint.xml.Results;
import de.lgohlke.sonar.maven.lint.xml.Violation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.maven.project.MavenProject;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.maven.DependsUponMavenPlugin;
import org.sonar.api.batch.maven.MavenPluginHandler;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issue;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.AbstractLanguage;
import org.sonar.api.resources.Project;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.Rule;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

@Rules(values = {
    LintDuplicateDependenciesRule.class,
    LintExecutionIdRule.class,
    LintGroupArtifactVersionMustBeInCorrectOrderIdRule.class,
    LintMissingCIManagementRule.class,
    LintMissingIssueManagementRule.class,
    LintMissingDeveloperInformationRule.class,
    LintRedundantDependencyVersionsRule.class,
    LintRedundantPluginVersionsRule.class,
    LintProfileMustOnlyAddModulesRule.class,
    LintVersionPropertiesMustUseDotVersionRule.class,
    LintVersionPropertiesMustUseProjectVersionRule.class
})
@RequiredArgsConstructor
@Slf4j
public class LintSensor implements DependsUponMavenPlugin, Sensor {
  private final static String LINT_FILENAME = "target/sonar-maven-lint." + System.currentTimeMillis() + ".xml";
  private final static Object BASE_PREFIX = "lint";
  private final MavenProject mavenProject;
  private final RulesProfile rulesProfile;
  private final ResourcePerspectives resourcePerspectives;
  private final Settings settings;

  @Override
  public void analyse(Project project, SensorContext context) {
    String xml = getXmlFromReport();
    Results results = getResults(xml);

    for (Violation violation : results.getViolations()) {
      Rule rule = createRuleFromViolation(violation);
      if (rule != null) {
        addIssue(violation, rule);
      }
    }
  }

  @VisibleForTesting
  void addIssue(Violation violation, Rule rule) {
    org.sonar.api.resources.File file = new org.sonar.api.resources.File("", mavenProject.getFile().getName());
    file.setLanguage(new AbstractLanguage("xml") {
      @Override
      public String[] getFileSuffixes() {
        return new String[]{"xml"};
      }
    });

    Issuable issuable = resourcePerspectives.as(Issuable.class, file);
    RuleKey ruleKey = RuleKey.of(rule.getRepositoryKey(), rule.getKey());

    Issue issue = issuable.newIssueBuilder().
        line(violation.getLocation().getLine() + 1).
        message(violation.getMessage()).
        ruleKey(ruleKey).
        build();

    issuable.addIssue(issue);
  }

  @VisibleForTesting
  Results getResults(String xml) {
    return new ResultsReader().read(xml);
  }

  @VisibleForTesting
  String getXmlFromReport() {
    final File projectDirectory = mavenProject.getOriginalModel().getPomFile().getParentFile();
    final File xmlReport = new File(projectDirectory, LINT_FILENAME);
    try {
      return FileUtils.readFileToString(xmlReport);
    } catch (IOException e) {
      LintSensor.log.error(e.getMessage(), e);
      throw new IllegalStateException(e);
    }
  }

  @VisibleForTesting
  Rule createRuleFromViolation(Violation violation) {
    final Class<? extends MavenRule>[] values = getClass().getAnnotation(Rules.class).values();
    for (Class<? extends MavenRule> ruleClazz : values) {
      org.sonar.check.Rule rule = ruleClazz.getAnnotation(org.sonar.check.Rule.class);
      if (rule.key().equals(BASE_PREFIX + "." + violation.getRule())) {
        return RuleUtils.createRuleFrom(ruleClazz);
      }
    }

    log.warn("rule for violation " + violation.getRule() + " is not implemented yet");
    return null;
  }

  @Override
  public MavenPluginHandler getMavenPluginHandler(final Project project) {
    final Properties mavenProjectProperties = mavenProject.getProperties();
    mavenProjectProperties.setProperty("maven-lint.failOnViolation", "false");
    mavenProjectProperties.setProperty("maven-lint.output.file.xml", LINT_FILENAME);
    return MavenPluginHandlerFactory.createHandler(de.lgohlke.sonar.maven.lint.Configuration.BASE_IDENTIFIER);
  }

  @Override
  public boolean shouldExecuteOnProject(Project project) {
    String prop = settings.getProperties().get(Configuration.ANALYSIS_ENABLED);
    if (prop == null) {
      prop = de.lgohlke.sonar.Configuration.DEFAULT;
    }

    boolean activatedByConfiguration = Boolean.parseBoolean(prop);
    boolean activatedByRules = checkIfAtLeastOneRuleIsEnabled();

    return activatedByConfiguration && activatedByRules;
  }

  private boolean checkIfAtLeastOneRuleIsEnabled() {
    List<Rule> associatedRules = getAssociatedRules();
    for (ActiveRule activeRule : rulesProfile.getActiveRules()) {
      if (associatedRules.contains(activeRule.getRule())) {
        return true;
      }
    }
    return false;
  }

  private List<Rule> getAssociatedRules() {
    List<Rule> rules = Lists.newArrayList();
    for (Class<? extends MavenRule> ruleClass : getClass().getAnnotation(Rules.class).values()) {
      rules.add(RuleUtils.createRuleFrom(ruleClass));
    }
    return rules;
  }
}
