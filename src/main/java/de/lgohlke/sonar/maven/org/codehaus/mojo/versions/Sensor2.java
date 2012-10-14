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
package de.lgohlke.sonar.maven.org.codehaus.mojo.versions;

import de.lgohlke.sonar.maven.internals.MavenPluginHandlerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.maven.DependsUponMavenPlugin;
import org.sonar.api.batch.maven.MavenPluginHandler;
import org.sonar.api.resources.Project;

import static de.lgohlke.sonar.maven.org.codehaus.mojo.versions.Configuration.BASE_IDENTIFIER;
import static de.lgohlke.sonar.maven.org.codehaus.mojo.versions.Configuration.Goals.DISPLAY_DEPENDENCY_UPDATES;


public class Sensor2 extends SensorBase implements Sensor, DependsUponMavenPlugin {


  @Override
  public MavenPluginHandler getMavenPluginHandler(final Project project) {
    return MavenPluginHandlerFactory.createHandler(BASE_IDENTIFIER + DISPLAY_DEPENDENCY_UPDATES);
  }

  public void analyse(Project project, SensorContext context) {
    System.out.println("analysing:" + this.toString());
  }

  public boolean shouldExecuteOnProject(Project project) {
    return true;
  }
}
