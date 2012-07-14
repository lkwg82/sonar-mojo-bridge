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

import org.apache.maven.execution.MavenSession;
import org.apache.maven.lifecycle.LifecycleExecutor;
import org.apache.maven.project.MavenProject;
import org.sonar.batch.MavenPluginExecutor;

import static org.fest.reflect.core.Reflection.field;

public class MavenPluginExecutorFactory {

  public static MavenPluginExecutorWithExecutionListener createInstance(final MavenProject mavenProject, final MavenPluginExecutor mavenPluginExecutor) {

    LifecycleExecutor lifecycleExecutor = field("lifecycleExecutor").
        ofType(LifecycleExecutor.class).
        in(mavenPluginExecutor).get();

    MavenSession mavenSession = field("mavenSession").
        ofType(MavenSession.class).
        in(mavenPluginExecutor).get();

    return new Maven3PluginExecutorWithExecutionListener(lifecycleExecutor, mavenSession);
  }
}
