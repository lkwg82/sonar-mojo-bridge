/*
 * sonar-maven-checks-maven-internals
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
import com.google.common.collect.Maps;
import de.lgohlke.sonar.Configuration;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.project.MavenProject;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.maven.DependsUponMavenPlugin;
import org.sonar.api.batch.maven.MavenPluginHandler;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.ActiveRuleParam;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleParam;
import org.sonar.batch.scan.maven.MavenPluginExecutor;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: lars
 */
@Data
@Slf4j
public abstract class MavenBaseSensor<T extends ResultTransferHandler> implements DependsUponMavenPlugin, Sensor {
  private final RulesProfile rulesProfile;
  private final MavenPluginExecutor mavenPluginExecutor;
  private final MavenProject mavenProject;
  @Getter
  private BridgeMojoMapper<T> mojoMapper;

  public MavenBaseSensor(final RulesProfile rulesProfile, final MavenPluginExecutor mavenPluginExecutor, final MavenProject mavenProject) {
    this.rulesProfile = rulesProfile;
    this.mavenPluginExecutor = mavenPluginExecutor;
    this.mavenProject = mavenProject;

    checkNotNull(getClass().getAnnotation(SensorConfiguration.class), "each sensor must have the annotation " + SensorConfiguration.class);
    checkNotNull(getClass().getAnnotation(Rules.class), "each sensor must have the annotation " + Rules.class);

    SensorConfiguration configuration = getClass().getAnnotation(SensorConfiguration.class);
    verifyConfiguration(configuration);

    Class<? extends BridgeMojo<T>> bridgeMojoClass = (Class<? extends BridgeMojo<T>>) configuration.bridgeMojo();
    try {
      T resultTransferHandler = (T) configuration.resultTransferHandler().newInstance();
      mojoMapper = new BridgeMojoMapper<T>(bridgeMojoClass, resultTransferHandler);
    } catch (InstantiationException e) {
      throw new IllegalStateException(e);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException(e);
    }
  }

  private static void verifyConfiguration(SensorConfiguration configuration) {
    final String identifier = configuration.mavenBaseIdentifier();
    checkArgument(identifier.length() > 0, "base identifier should not be empty");
    checkArgument(identifier.matches("[^:]+:[^:]+:[^:]+:"), "base identifier should match 'group:artifact:version:' (excluding goal)");
  }

  public boolean shouldExecuteOnProject(final Project project) {
    String prop = (String) project.getProperty(Configuration.ANALYSIS_ENABLED);
    if (prop == null) {
      prop = de.lgohlke.sonar.Configuration.DEFAULT;
    }

    boolean activatedByConfiguration = Boolean.parseBoolean(prop);
    boolean activatedByRules = checkIfAtLeastOneRuleIsEnabled();

    boolean isActivated = activatedByConfiguration && activatedByRules;
    boolean isMaven3 = MavenPluginExecutorProxyInjection.checkIfIsMaven3(mavenPluginExecutor);

    if (isActivated) {
      if (isMaven3) {
        MavenPluginExecutorProxyInjection.inject(mavenPluginExecutor, getClass().getClassLoader(), mojoMapper);
      } else {
        MavenBaseSensor.log.warn("this plugin is incompatible with maven2, run again with maven3");
      }
    }

    return isActivated && isMaven3;
  }

  protected boolean checkIfAtLeastOneRuleIsEnabled() {
    List<Rule> associatedRules = getAssociatedRules();
    for (ActiveRule activeRule : rulesProfile.getActiveRules()) {
      if (associatedRules.contains(activeRule.getRule())) {
        return true;
      }
    }
    return false;
  }

  protected List<Rule> getAssociatedRules() {
    List<Rule> rules = Lists.newArrayList();
    for (Class<? extends MavenRule> ruleClass : getClass().getAnnotation(Rules.class).values()) {
      rules.add(RuleUtils.createRuleFrom(ruleClass));
    }
    return rules;
  }

  @Override
  public MavenPluginHandler getMavenPluginHandler(final Project project) {
    String baseIdentifier = getClass().getAnnotation(SensorConfiguration.class).mavenBaseIdentifier();
    return MavenPluginHandlerFactory.createHandler(baseIdentifier + mojoMapper.getGoal());
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  public abstract void analyse(final Project project, final SensorContext context);

  protected Map<String, String> createRulePropertiesMapFromQualityProfile(Class<? extends MavenRule> ruleClass) {
    Map<String, String> mappedParams = Maps.newHashMap();
    String ruleKey = RuleUtils.createRuleFrom(ruleClass).getKey();
    ActiveRule activeRuleByConfigKey = getRulesProfile().getActiveRuleByConfigKey(Configuration.REPOSITORY_KEY, ruleKey);
    if (null != activeRuleByConfigKey) {
      List<ActiveRuleParam> activeRuleParams = activeRuleByConfigKey.getActiveRuleParams();
      for (ActiveRuleParam activeRuleParam : activeRuleParams) {
        mappedParams.put(activeRuleParam.getKey(), activeRuleParam.getValue());
      }

      // fill with default values for params not set
      final List<RuleParam> params = activeRuleByConfigKey.getRule().getParams();
      for (RuleParam ruleParam : params) {
        if (!mappedParams.containsKey(ruleParam.getKey())) {
          mappedParams.put(ruleParam.getKey(), ruleParam.getDefaultValue());
        }
      }
    }
    return mappedParams;
  }
}
