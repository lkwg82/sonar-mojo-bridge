/*
 * Sonar maven checks plugin
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
package de.lgohlke.sonar.maven;

import com.google.common.collect.Lists;
import de.lgohlke.sonar.MavenPlugin;
import de.lgohlke.sonar.MavenRule;
import de.lgohlke.sonar.maven.internals.MavenPluginExecutorProxyInjection;
import de.lgohlke.sonar.maven.internals.MavenPluginHandlerFactory;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.project.MavenProject;
import org.sonar.api.batch.maven.DependsUponMavenPlugin;
import org.sonar.api.batch.maven.MavenPluginHandler;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleRepository;
import org.sonar.batch.MavenPluginExecutor;

import java.util.Arrays;
import java.util.List;

/**
 * User: lars
 */
@RequiredArgsConstructor
@Data
@Slf4j
public class MavenBaseSensor<T extends ResultTransferHandler> implements DependsUponMavenPlugin {
  private final RulesProfile rulesProfile;
  private final MavenPluginExecutor mavenPluginExecutor;
  private final MavenProject mavenProject;
  private final String baseIdentifier;
  private final MavenBaseSensorI<T> mavenBaseSensorI;

  public boolean shouldExecuteOnProject(final Project project) {
    String prop = (String) project.getProperty(MavenPlugin.ANALYSIS_ENABLED);
    if (prop == null) {
      prop = MavenPlugin.DEFAULT;
    }

    boolean isMaven3 = MavenPluginExecutorProxyInjection.checkIfIsMaven3(mavenPluginExecutor);
    if (isMaven3) {
      MavenPluginExecutorProxyInjection.inject(mavenPluginExecutor, getClass().getClassLoader(), getHandler());
    } else {
      MavenBaseSensor.log.warn("this plugin is incompatible with maven2, run again with maven3");
    }

    return Boolean.parseBoolean(prop) && isMaven3 && checkIfAtLeastOneRuleIsEnabled();
  }

  private boolean checkIfAtLeastOneRuleIsEnabled() {
    List<String> associatedRules = getAssociatedRules();
    for (ActiveRule rule : rulesProfile.getActiveRules()) {
      Rule innerRule = rule.getRule();
      if (MavenPlugin.REPOSITORY_KEY.equals(innerRule.getRepositoryKey()) && associatedRules.contains(innerRule.getKey())) {
        return true;
      }
    }
    return false;
  }

  public List<String> getAssociatedRules() {
    List<Class<? extends MavenRule>> rules = Arrays.asList(mavenBaseSensorI.getClass().getAnnotation(Rules.class).values());
    List<String > ruleKeys = Lists.newArrayList();
    for(Class<? extends MavenRule> rule : rules){
                        ruleKeys.add(rule.getAnnotation(org.sonar.check.Rule.class).key());
    }
    return ruleKeys;
  }

  @Override
  public MavenPluginHandler getMavenPluginHandler(final Project project) {
    return MavenPluginHandlerFactory.createHandler(baseIdentifier + getHandler().getGoal());
  }

  public BridgeMojoMapper<T> getHandler() {
    return mavenBaseSensorI.getHandler();
  }

  public String toString() {
    return mavenBaseSensorI.getClass().getSimpleName();
  }

  public Rule createRuleFrom(Class<? extends MavenRule> ruleClass) {
    String key = ruleClass.getAnnotation(org.sonar.check.Rule.class).key();
    return Rule.create(MavenPlugin.REPOSITORY_KEY, key);
  }
}
