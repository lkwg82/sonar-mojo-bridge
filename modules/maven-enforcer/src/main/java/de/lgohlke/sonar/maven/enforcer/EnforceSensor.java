/*
 * sonar-mojo-bridge-maven-enforcer
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
package de.lgohlke.sonar.maven.enforcer;

import de.lgohlke.sonar.maven.*;
import de.lgohlke.sonar.maven.enforcer.DependencyConvergence.DependencyConvergenceRule;
import org.apache.maven.project.MavenProject;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.maven.MavenPluginHandler;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.Rule;
import org.sonar.batch.scan.maven.MavenPluginExecutor;

import java.util.List;

import static de.lgohlke.sonar.maven.enforcer.Configuration.BASE_IDENTIFIER;
import static org.fest.reflect.core.Reflection.constructor;

@Rules(values = {DependencyConvergenceRule.class})
@SensorConfiguration(
    bridgeMojo = EnforceBridgeMojo.class, //
    resultTransferHandler = RuleTransferHandler.class, //
    mavenBaseIdentifier = BASE_IDENTIFIER
)
public class EnforceSensor extends MavenBaseSensor<RuleTransferHandler> {
  private final EnforceMavenPluginHandler mavenPluginHandler;

  public EnforceSensor(RulesProfile rulesProfile, MavenPluginExecutor mavenPluginExecutor, MavenProject mavenProject, Project project, ResourcePerspectives resourcePerspectives) {
    super(rulesProfile, mavenPluginExecutor, mavenProject, resourcePerspectives);
    this.mavenPluginHandler = new EnforceMavenPluginHandler(super.getMavenPluginHandler(project));

    configureMavenPluginHandler(rulesProfile, mavenProject);
  }

  private void configureMavenPluginHandler(RulesProfile rulesProfile, MavenProject mavenProject) {
    for (Class<? extends MavenRule> ruleClass : getClass().getAnnotation(Rules.class).values()) {
      Rule rule = RuleUtils.createRuleFrom(ruleClass);
      for (ActiveRule activeRule : rulesProfile.getActiveRules()) {
        if (rule.equals(activeRule.getRule())) {
          initEnforcerRule(mavenProject, ruleClass);
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void initEnforcerRule(final MavenProject mavenProject, final Class<? extends MavenRule> ruleClass) {
    Class<? extends EnforcerRule> aClass = Configuration.RULE_ADAPTER_MAP.get(ruleClass);
    EnforcerRule enforcerRule = constructor().in(aClass).newInstance();

    getMojoMapper().getResultTransferHandler().getRules().add(enforcerRule);
    enforcerRule.configure(mavenPluginHandler);

    Class<ViolationAdapter> violationAdapterClass = enforcerRule.getViolationAdapterClass();
    ViolationAdapter adapter = constructor().withParameterTypes(MavenProject.class).in(violationAdapterClass).newInstance(mavenProject);

    enforcerRule.setViolationAdapter(adapter);
  }

  @Override
  public boolean shouldExecuteOnProject(final Project project) {
    boolean rulesEmpty = getMojoMapper().getResultTransferHandler().getRules().isEmpty();
    return !rulesEmpty && super.shouldExecuteOnProject(project);
  }

  @Override
  public void analyse(Project project, SensorContext context) {
    List<EnforcerRule> rules = getMojoMapper().getResultTransferHandler().getRules();
    for (EnforcerRule rule : rules) {
      ViolationAdapter violationAdapter = rule.getViolationAdapter();
      for (Violation violation : violationAdapter.getViolations()) {
        addIssue(violation.getMessage(), violation.getLine(), violation.getRule());
      }
    }
  }

  @Override
  public MavenPluginHandler getMavenPluginHandler(Project project) {
    return mavenPluginHandler;
  }
}
