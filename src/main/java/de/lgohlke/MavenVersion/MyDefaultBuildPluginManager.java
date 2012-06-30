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

import com.thoughtworks.xstream.InitializationException;
import hudson.maven.MavenEmbedder;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.DefaultBuildPluginManager;
import org.apache.maven.plugin.LegacySupport;
import org.apache.maven.plugin.MavenPluginManager;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.PluginConfigurationException;
import org.apache.maven.plugin.PluginContainerException;
import org.apache.maven.plugin.PluginExecutionException;
import org.apache.maven.plugin.PluginManagerException;
import org.apache.maven.plugin.PluginResolutionException;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;

public class MyDefaultBuildPluginManager extends DefaultBuildPluginManager {

  private MavenPluginManager mavenPluginManager;

  private LegacySupport legacySupport;
  private boolean initialized = false;

  private final MojoExecutionHandler mojoExecutionHandler;

  public MyDefaultBuildPluginManager(final MavenEmbedder embedder, final MojoExecutionHandler handler) {
    this.mojoExecutionHandler = handler;
    try {
      mavenPluginManager = embedder.lookup(MavenPluginManager.class);
      legacySupport = embedder.lookup(LegacySupport.class);

    } catch (ComponentLookupException e) {
      throw new IllegalStateException(e);
    }
  }

  private void setPrivateFieldOfSuperClass(final String fieldName, final Object value) throws NoSuchFieldException, SecurityException, IllegalArgumentException,
      IllegalAccessException {
    Field f = getClass().getSuperclass().getDeclaredField(fieldName);
    try {
      f.setAccessible(true);
      f.set(this, value);
    } finally {
      f.setAccessible(false);
    }
  }

  public MyDefaultBuildPluginManager init() throws InitializationException {

    try {
      setPrivateFieldOfSuperClass("mavenPluginManager", mavenPluginManager);
      setPrivateFieldOfSuperClass("legacySupport", legacySupport);
      initialized = true;
    } catch (NoSuchFieldException e) {
      throw new InitializationException(e.getMessage(), e);
    } catch (SecurityException e) {
      throw new InitializationException(e.getMessage(), e);
    } catch (IllegalArgumentException e) {
      throw new InitializationException(e.getMessage(), e);
    } catch (IllegalAccessException e) {
      throw new InitializationException(e.getMessage(), e);
    }
    return this;
  }

  // ----------------------------------------------------------------------
  // Mojo execution
  // ----------------------------------------------------------------------

  @Override
  public void executeMojo(final MavenSession session, final MojoExecution mojoExecution) throws MojoFailureException, MojoExecutionException, PluginConfigurationException,
      PluginManagerException
  {
    if (initialized) {
      doExecuteMojo(session, mojoExecution);
    } else {
      throw new IllegalStateException("please call init() as first");
    }
  }

  /**
   * this is almost completely copied from {#link {@link DefaultBuildPluginManager#executeMojo(MavenSession, MojoExecution)}}
   * except some hook methods for the handler
   * @param session
   * @param mojoExecution
   * @throws PluginManagerException
   * @throws PluginConfigurationException
   * @throws MojoExecutionException
   * @throws MojoFailureException
   */
  private void doExecuteMojo(final MavenSession session, final MojoExecution mojoExecution) throws PluginManagerException, PluginConfigurationException, MojoExecutionException,
      MojoFailureException {
    MavenProject project = session.getCurrentProject();

    MojoDescriptor mojoDescriptor = mojoExecution.getMojoDescriptor();

    Mojo mojo = null;

    ClassRealm pluginRealm;
    try
    {
      pluginRealm = getPluginRealm(session, mojoDescriptor.getPluginDescriptor());
    } catch (PluginResolutionException e)
    {
      throw new PluginExecutionException(mojoExecution, project, e);
    }

    ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(pluginRealm);

    MavenSession oldSession = legacySupport.getSession();

    try
    {
      mojo = mavenPluginManager.getConfiguredMojo(Mojo.class, session, mojoExecution);

      legacySupport.setSession(session);

      // NOTE: DuplicateArtifactAttachmentException is currently unchecked, so be careful removing this try/catch!
      // This is necessary to avoid creating compatibility problems for existing plugins that use
      // MavenProjectHelper.attachArtifact(..).
      try
      {
        mojoExecutionHandler.beforeExecution(mojo);
        mojo.execute();
        mojoExecutionHandler.afterExecution(mojo);
      } catch (ClassCastException e)
      {
        // to be processed in the outer catch block
        throw e;
      } catch (RuntimeException e)
      {
        throw new PluginExecutionException(mojoExecution, project, e);
      }
    } catch (PluginContainerException e)
    {
      throw new PluginExecutionException(mojoExecution, project, e);
    } catch (NoClassDefFoundError e)
    {
      ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
      PrintStream ps = new PrintStream(os);
      ps.println("A required class was missing while executing " + mojoDescriptor.getId() + ": "
        + e.getMessage());
      pluginRealm.display(ps);

      Exception wrapper = new PluginContainerException(mojoDescriptor, pluginRealm, os.toString(), e);

      throw new PluginExecutionException(mojoExecution, project, wrapper);
    } catch (LinkageError e)
    {
      ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
      PrintStream ps = new PrintStream(os);
      ps.println("An API incompatibility was encountered while executing " + mojoDescriptor.getId() + ": "
        + e.getClass().getName() + ": " + e.getMessage());
      pluginRealm.display(ps);

      Exception wrapper = new PluginContainerException(mojoDescriptor, pluginRealm, os.toString(), e);

      throw new PluginExecutionException(mojoExecution, project, wrapper);
    } catch (ClassCastException e)
    {
      ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
      PrintStream ps = new PrintStream(os);
      ps.println("A type incompatibility occured while executing " + mojoDescriptor.getId() + ": "
        + e.getMessage());
      pluginRealm.display(ps);

      throw new PluginExecutionException(mojoExecution, project, os.toString(), e);
    } finally
    {
      mavenPluginManager.releaseMojo(mojo, mojoExecution);

      Thread.currentThread().setContextClassLoader(oldClassLoader);

      legacySupport.setSession(oldSession);
    }
  }

}
