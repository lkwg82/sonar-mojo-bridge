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
package de.lgohlke.sonar.maven.plugin.versions;

import de.lgohlke.sonar.maven.MavenPluginExecutorFactory;
import de.lgohlke.sonar.plugin.MavenPlugin;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Phase;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.bootstrap.ProjectDefinition;
import org.sonar.api.batch.maven.DependsUponMavenPlugin;
import org.sonar.api.batch.maven.MavenPluginHandler;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.batch.MavenPluginExecutor;

@Phase(name = Phase.Name.DEFAULT)
public class MavenVersionSensor implements Sensor, DependsUponMavenPlugin {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  // private final List<? extends MavenGoalExecutor> executors = Arrays.asList(new DependencyVersionExecutor());
  private final MavenProject mavenProject;
  private final RulesProfile rulesProfile;

  private final MavenPluginExecutor mavenPluginExecutor;

  private final ProjectDefinition projectDefinition;

  private final Project project;

  public MavenVersionSensor(final MavenProject mavenProject, final RulesProfile profile, final MavenPluginExecutor mavenPluginExecutor, final Project project,
      final ProjectDefinition projectDefinition) {
    this.mavenProject = mavenProject;
    this.rulesProfile = profile;
    this.project = project;
    this.projectDefinition = projectDefinition;
    this.mavenPluginExecutor = MavenPluginExecutorFactory.createInstance(mavenProject, mavenPluginExecutor);
  }

  public MavenVersionSensor() {
    this(null, null, null, null, null);
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

    final MavenPluginHandler handler = getMavenPluginHandler(project);
    mavenPluginExecutor.execute(project, projectDefinition, handler);

    // for (MavenGoalExecutor executor : executors) {
    // logger.debug("checking if executor {} needs to be executed", executor.getClass());
    // executor.setRulesProfile(rulesProfile);
    // if (executor.needsToBeExecuted()) {
    // logger.debug("executing", executor.getClass());
    // executor.execute(mavenProject, context);
    // }
    // }
    // for (GOAL goal : GOAL.values()) {
    // if (isCurrentRuleActive(goal.rule())) {
    // executeGoalForRule(context, goal);
    // } else {
    // logger.info("skipping for " + goal.goal() + " rule inactive");
    // }
    // }
  }

  @Override
  public MavenPluginHandler getMavenPluginHandler(final Project project) {
    return new MavenPluginHandler() {

      @Override
      public boolean isFixedVersion() {
        return true;
      }

      @Override
      public String getVersion() {
        return "1.3.1";
      }

      @Override
      public String getGroupId() {
        return "org.codehaus.mojo";
      }

      @Override
      public String[] getGoals() {
        return new String[] {"help"};
      }

      @Override
      public String getArtifactId() {
        return "versions-maven-plugin";
      }

      @Override
      public void configure(final Project project, final org.sonar.api.batch.maven.MavenPlugin plugin) {
        // ko
      }
    };
  }

  // private void executeGoalForRule(final SensorContext context, final GOAL goal) {
  //
  // try {
  // UpdateHandler handler = goal.handler().newInstance();
  // Log.info("testing for " + goal.goal());
  // new MavenInvoker(mavenProject.getFile(), handler).run(goal);
  //
  // Rule rule = Rule.create(MavenPlugin.REPOSITORY_KEY, goal.rule().getKey());
  // final File file = new File("", mavenProject.getFile().getName());
  // file.setLanguage(Xml.INSTANCE);
  // for (ArtifactUpdate update : handler.getUpdates()) {
  // Violation violation = Violation.create(rule, file);
  // violation.setMessage(goal.rule().formatMessage(update));
  // context.saveViolation(violation);
  // }
  // } catch (InstantiationException e) {
  // logger.error(e.getMessage(), e);
  // } catch (IllegalAccessException e) {
  // logger.error(e.getMessage(), e);
  // } catch (MavenInvocationException e) {
  // logger.error(e.getMessage(), e);
  // }
  // }
}
