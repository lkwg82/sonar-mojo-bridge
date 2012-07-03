/*
 * Sonar maven checks plugin
 * Copyright (C) 2012 ${owner}
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
package de.lgohlke.sonar.maven.plugin.versions;

import de.lgohlke.sonar.maven.MavenInvoker;
import de.lgohlke.sonar.maven.handler.ArtifactUpdate;
import de.lgohlke.sonar.maven.handler.GOAL;
import de.lgohlke.sonar.maven.handler.UpdateHandler;

import de.lgohlke.sonar.plugin.MavenRule;

import de.lgohlke.sonar.plugin.MavenPlugin;

import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Phase;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.File;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.Violation;
import org.sonar.plugins.xml.language.Xml;

@Phase(name = Phase.Name.DEFAULT)
public class MavenVersionSensor implements Sensor {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final MavenProject mavenProject;
  private final RulesProfile rulesProfile;

  public MavenVersionSensor(final MavenProject mavenProject, final RulesProfile profile) {
    this.mavenProject = mavenProject;
    this.rulesProfile = profile;
  }

  public MavenVersionSensor() {
    this(null, null);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  @Override
  public boolean shouldExecuteOnProject(final Project project) {
    String prop = (String) project.getProperty(MavenPlugin.ANALYSIS_ENABLED);
    if (prop == null) {

      prop = MavenPlugin.DEFAULT;
    }
    final boolean propActive = Boolean.parseBoolean(prop);
    return propActive && mavenProject != null;
  }

  @Override
  public void analyse(final Project project, final SensorContext context) {

    for (GOAL goal : GOAL.values()) {
      if (isCurrentRuleActive(goal.rule())) {
        executeGoalForRule(context, goal);
      } else {
        logger.info("skipping for " + goal.goal() + " rule inactive");
      }
    }
  }

  private void executeGoalForRule(final SensorContext context, final GOAL goal) {

    try {
      UpdateHandler handler = goal.handler().newInstance();
      Log.info("testing for " + goal.goal());
      new MavenInvoker(mavenProject.getFile(), handler).run(goal);

      Rule rule = Rule.create(MavenPlugin.REPOSITORY_KEY, goal.rule().getKey());
      final File file = new File("", mavenProject.getFile().getName());
      file.setLanguage(Xml.INSTANCE);
      for (ArtifactUpdate update : handler.getUpdates()) {
        Violation violation = Violation.create(rule, file);
        violation.setMessage(goal.rule().formatMessage(update));
        context.saveViolation(violation);
      }
    } catch (InstantiationException e) {
      logger.error(e.getMessage(), e);
    } catch (IllegalAccessException e) {
      logger.error(e.getMessage(), e);
    } catch (MavenInvocationException e) {
      logger.error(e.getMessage(), e);
    }
  }

  private boolean isCurrentRuleActive(final MavenRule rule) {
    for (ActiveRule activeRule : rulesProfile.getActiveRules()) {
      if (activeRule.getConfigKey().equals(rule.getKey()) && activeRule.getRepositoryKey().equals(MavenPlugin.REPOSITORY_KEY)) {
        return true;
      }
    }
    return false;
  }
}
