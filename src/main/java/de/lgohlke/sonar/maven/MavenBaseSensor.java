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

import de.lgohlke.sonar.maven.internals.MavenPluginExecutorProxyInjection;
import de.lgohlke.sonar.MavenPlugin;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.project.MavenProject;
import org.sonar.api.batch.Phase;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.maven.DependsUponMavenPlugin;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.batch.MavenPluginExecutor;


@Data
@Phase(name = Phase.Name.PRE)
@RequiredArgsConstructor
@Slf4j
public abstract class MavenBaseSensor<T extends ResultTransferHandler> implements Sensor, DependsUponMavenPlugin {
  private final RulesProfile rulesProfile;
  private final MavenPluginExecutor mavenPluginExecutor;
  private final MavenProject mavenProject;

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

    boolean isMaven3 = MavenPluginExecutorProxyInjection.checkIfIsMaven3(mavenPluginExecutor);
    if (isMaven3) {
      MavenPluginExecutorProxyInjection.inject(mavenPluginExecutor, getClass().getClassLoader(), getHandler());
    } else {
      log.warn("this plugin is incompatible with maven2, run again with maven3");
    }

    return Boolean.parseBoolean(prop) && isMaven3;
  }

  protected abstract BridgeMojoMapper<T> getHandler();
}
