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

import org.apache.maven.execution.ExecutionListener;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.lifecycle.LifecycleExecutor;
import org.apache.maven.project.MavenProject;
import org.sonar.api.utils.SonarException;
import org.sonar.batch.AbstractMavenPluginExecutor;

import java.util.Arrays;

class Maven3PluginExecutorWithExecutionListener extends AbstractMavenPluginExecutor implements MavenPluginExecutorWithExecutionListener {

  private final LifecycleExecutor lifecycleExecutor;
  private final MavenSession mavenSession;
  private ExecutionListener executionListener;

  public Maven3PluginExecutorWithExecutionListener(final LifecycleExecutor le, final MavenSession mavenSession) {
    this.lifecycleExecutor = le;
    this.mavenSession = mavenSession;
  }

  @Override
  public void concreteExecute(final MavenProject pom, final String goal) {
    MavenSession projectSession = mavenSession.clone();
    projectSession.setCurrentProject(pom);
    projectSession.setProjects(Arrays.asList(pom));
    projectSession.getRequest().setRecursive(false);
    projectSession.getRequest().setExecutionListener(executionListener);
    projectSession.getRequest().setPom(pom.getFile());
    projectSession.getRequest().setGoals(Arrays.asList(goal));
    projectSession.getRequest().setInteractiveMode(false);
    lifecycleExecutor.execute(projectSession);
    if (projectSession.getResult().hasExceptions()) {
      throw new SonarException("Exception during execution of " + goal);
    }
  }

  @Override
  public void setExecutionListener(final ExecutionListener executionListener) {
    this.executionListener = executionListener;
  }

}
