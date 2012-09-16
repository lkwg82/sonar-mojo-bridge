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

import de.lgohlke.sonar.maven.MavenPluginExecutorProxyInjection;
import de.lgohlke.sonar.maven.plugin.versions.bridgeMojos.DisplayDependencyUpdatesBridgeMojoResultHandler;
import de.lgohlke.sonar.plugin.MavenPlugin;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Phase;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.maven.DependsUponMavenPlugin;
import org.sonar.api.batch.maven.MavenPluginHandler;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.batch.MavenPluginExecutor;
import org.sonar.maven3.Maven3PluginExecutor;

@Phase(name = Phase.Name.PRE)

public class MavenVersionSensor implements Sensor, DependsUponMavenPlugin {
  private final static Logger log = LoggerFactory.getLogger(MavenVersionSensor.class);
  private final RulesProfile rulesProfile;
  private final MavenVersionsBridgeMojoMapper bridgeMojoMapper = new MavenVersionsBridgeMojoMapper();
  private final MavenProject mavenProject;
  final boolean isMaven3;

  public MavenVersionSensor(final RulesProfile profile, final MavenPluginExecutor mavenPluginExecutor, final MavenProject mavenProject) {
    this.rulesProfile = profile;
    MavenPluginExecutorProxyInjection.inject(mavenPluginExecutor, getClass().getClassLoader(), bridgeMojoMapper);
    this.mavenProject = mavenProject;

    if (mavenPluginExecutor instanceof Maven3PluginExecutor) {
      isMaven3 = true;
    } else {
      isMaven3 = false;
    }
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

    if (!isMaven3) {
      log.warn("this plugin is incompatible with maven2, run again with maven3");
    }

    return Boolean.parseBoolean(prop) && isMaven3;
  }

  @Override
  public void analyse(final Project project, final SensorContext context) {

    DisplayDependencyUpdatesBridgeMojoResultHandler handler = (DisplayDependencyUpdatesBridgeMojoResultHandler) bridgeMojoMapper.getGoalToTransferHandlerMap().get(
        Goals.DISPLAY_DEPENDENCY_UPDATES);

    handler.setMavenProject(mavenProject);
    handler.analyse(project, context);
  }

  @Override
  public MavenPluginHandler getMavenPluginHandler(final Project project) {
    return MavenVersionsPluginHandlerFactory.create(MavenVersionsGoal.DisplayDependencyUpdates);
  }
}
