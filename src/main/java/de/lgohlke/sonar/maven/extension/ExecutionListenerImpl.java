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
package de.lgohlke.sonar.maven.extension;

import de.lgohlke.sonar.maven.MavenPluginExecutorWithExecutionListener;
import de.lgohlke.sonar.maven.MojoExecutionHandler;
import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.execution.ExecutionListener;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.codehaus.plexus.component.repository.ComponentDescriptor;

public class ExecutionListenerImpl implements ExecutionListener {

  private final MojoExecutionHandler<?, ?> mojoExectionHandler;
  private final MavenPluginExecutorWithExecutionListener mavenPluginExecutor;

  public ExecutionListenerImpl(final MojoExecutionHandler<?, ?> mojoExectionHandler, final MavenPluginExecutorWithExecutionListener mavenPluginExecutor) {
    this.mojoExectionHandler = mojoExectionHandler;
    this.mavenPluginExecutor = mavenPluginExecutor;
  }

  @Override
  public void sessionStarted(final ExecutionEvent event) {
    // ok
  }

  @Override
  public void sessionEnded(final ExecutionEvent event) {
    // ok
  }

  @Override
  public void projectSucceeded(final ExecutionEvent event) {
    // ok
  }

  @Override
  public void projectStarted(final ExecutionEvent event) {
    // ok
  }

  @Override
  public void projectSkipped(final ExecutionEvent event) {
    // ok
  }

  @Override
  public void projectFailed(final ExecutionEvent event) {
    // ok
  }

  @Override
  public void projectDiscoveryStarted(final ExecutionEvent event) {
    // ok
  }

  @Override
  public void mojoSucceeded(final ExecutionEvent event) {
    // ok
  }

  @Override
  public void mojoStarted(final ExecutionEvent event) {
    handleMojo(event);
  }

  @Override
  public void mojoSkipped(final ExecutionEvent event) {
    // ok
  }

  @Override
  public void mojoFailed(final ExecutionEvent event) {
    // ok
  }

  @Override
  public void forkedProjectSucceeded(final ExecutionEvent event) {
    // ok
  }

  @Override
  public void forkedProjectStarted(final ExecutionEvent event) {
    // ok
  }

  @Override
  public void forkedProjectFailed(final ExecutionEvent event) {
    // ok
  }

  @Override
  public void forkSucceeded(final ExecutionEvent event) {
    // ok
  }

  @Override
  public void forkStarted(final ExecutionEvent event) {
    // ok
  }

  @Override
  public void forkFailed(final ExecutionEvent event) {
    // ok
  }

  public void handleMojo(final ExecutionEvent event) {
    try {

      MojoExecution mojoExecution = event.getMojoExecution();
      MojoDescriptor mojoDescriptor = mojoExecution.getMojoDescriptor();
      manipulatePluginDescriptor(mojoDescriptor.getPluginDescriptor());

      new MyDefaultBuildPluginManager(mavenPluginExecutor, mojoExectionHandler).
          init().
          executeMojo(event.getSession(), mojoExecution);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
    throw new StopMavenExectionException("this is correct flow, just to deny executing twice");
  }

  private void manipulatePluginDescriptor(final PluginDescriptor pluginDescriptor) {

    final String canonicalNameOfOriginalMojo = mojoExectionHandler.getOriginalMojo().getCanonicalName();
    for (ComponentDescriptor<?> c : pluginDescriptor.getComponents()) {
      // change mapping
      if (canonicalNameOfOriginalMojo.equals(c.getImplementation())) {
        c.setImplementationClass(mojoExectionHandler.getReplacingMojo());
      }
    }
  }
}
