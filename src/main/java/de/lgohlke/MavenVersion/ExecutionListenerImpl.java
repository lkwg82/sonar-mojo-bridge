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
package de.lgohlke.MavenVersion;

import hudson.maven.MavenEmbedder;
import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.execution.ExecutionListener;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

import java.util.Map;

public class ExecutionListenerImpl implements ExecutionListener {

  protected MavenEmbedder embedder;
  private final MojoExecutionHandler mojoExectionHandler;

  public ExecutionListenerImpl(final MojoExecutionHandler mojoExectionHandler) {
    this.mojoExectionHandler = mojoExectionHandler;
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
    System.out.println(event);
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

  public void setEmbedder(final MavenEmbedder embedder) {
    this.embedder = embedder;
  }

  public Object lookup(final Class<?> role) throws ComponentLookupException {
    return embedder.lookup(role);
  }

  public void handleMojo(final ExecutionEvent event) {
    try {

      MojoExecution mojoExecution = event.getMojoExecution();
      MojoDescriptor mojoDescriptor = mojoExecution.getMojoDescriptor();
      manipulatePluginDescriptor(mojoDescriptor.getPluginDescriptor(), mojoExectionHandler.getMojoMapping());

      new MyDefaultBuildPluginManager(embedder, mojoExectionHandler).
          init().
          executeMojo(event.getSession(), mojoExecution);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
    throw new StopMavenExectionException("this is correct flow, just to deny executing twice");
  }

  private void manipulatePluginDescriptor(final PluginDescriptor pluginDescriptor, final Map<String, Class<? extends Mojo>> mojoMapping) {

    for (ComponentDescriptor<?> c : pluginDescriptor.getComponents()) {
      System.out.println(c.getImplementation());
      // change mapping
      if (mojoMapping.containsKey(c.getImplementation())) {
        c.setImplementationClass(mojoMapping.get(c.getImplementation()));
      }
    }
  }
}
