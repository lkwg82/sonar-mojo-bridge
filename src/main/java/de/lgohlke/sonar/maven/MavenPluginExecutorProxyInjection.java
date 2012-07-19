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

import de.lgohlke.sonar.maven.plugin.versions.bridgeMojos.DisplayDependencyUpdatesBridgeMojo;

import de.lgohlke.sonar.maven.extension.DynamicProxy;
import de.lgohlke.sonar.maven.extension.MavenPluginManagerProxy;
import de.lgohlke.sonar.maven.extension.PlexusContainerProxy;
import de.lgohlke.sonar.maven.plugin.versions.BridgeMojoMapper;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MavenPluginManager;
import org.apache.maven.plugin.Mojo;
import org.codehaus.mojo.versions.DisplayDependencyUpdatesMojo;
import org.codehaus.plexus.PlexusContainer;
import org.sonar.batch.MavenPluginExecutor;
import org.sonar.maven3.Maven3PluginExecutor;

import java.lang.reflect.Proxy;

import static org.fest.reflect.core.Reflection.field;

public class MavenPluginExecutorProxyInjection {

  public static void inject(final MavenPluginExecutor mavenPluginExecutor, final ClassLoader classLoader, final BridgeMojoMapper handler) {
    try {
      if (mavenPluginExecutor instanceof Maven3PluginExecutor) {
        decorateMaven3ExecutionProcess(mavenPluginExecutor, classLoader, handler);
      }
    } catch (NoClassDefFoundError e) {
      decorateMaven2ExecutionProcess(mavenPluginExecutor, classLoader, handler);
    }
  }

  private static void decorateMaven2ExecutionProcess(final MavenPluginExecutor mavenPluginExecutor, final ClassLoader classLoader, final BridgeMojoMapper handler) {
    System.out.println(mavenPluginExecutor);
  }

  private static void decorateMaven3ExecutionProcess(final MavenPluginExecutor mavenPluginExecutor, final ClassLoader classLoader, final BridgeMojoMapper handler) {
    try {
      MavenSession mavenSession = field("mavenSession").ofType(MavenSession.class).in(mavenPluginExecutor).get();
      PlexusContainer container = field("container").ofType(PlexusContainer.class).in(mavenSession).get();
      MavenPluginManager mavenPluginManager = container.lookup(MavenPluginManager.class);
      BuildPluginManager pluginManager = container.lookup(BuildPluginManager.class);
      field("container").ofType(PlexusContainer.class).in(mavenPluginManager).set(getPlexusContainerProxy(PlexusContainer.class, container, handler));
      mavenPluginManager = getMavenPluginManagerProxy(MavenPluginManager.class, mavenPluginManager, classLoader);
      field("mavenPluginManager").ofType(MavenPluginManager.class).in(pluginManager).set(mavenPluginManager);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  public static <T extends MavenPluginManager> T getMavenPluginManagerProxy(final Class<T> intf, final T obj, final ClassLoader cl) {
    return newInstance(obj, intf, new MavenPluginManagerProxy<T>(obj, cl));
  }

  public static <T extends PlexusContainer> T getPlexusContainerProxy(final Class<T> intf, final T obj, final BridgeMojoMapper handler) {
    return newInstance(obj, intf, new PlexusContainerProxy<T>(obj, handler));
  }

  @SuppressWarnings("unchecked")
  private static <T> T newInstance(final Object obj, final Class<T> interfaze, final DynamicProxy<?> proxy) {
    return (T) Proxy.newProxyInstance(obj.getClass().getClassLoader(), new Class<?>[] {interfaze}, proxy);
  }
}

class MyMojoExecutionHandler extends MojoExecutionHandler<DisplayDependencyUpdatesMojo, DisplayDependencyUpdatesBridgeMojo> {

  @Override
  protected void beforeExecution2(final DisplayDependencyUpdatesBridgeMojo mojo) {
    System.out.println(mojo);
  }

  @Override
  protected void afterExecution2(final DisplayDependencyUpdatesBridgeMojo mojo) {
    System.out.println(mojo);
  }

  @Override
  public Class<DisplayDependencyUpdatesMojo> getOriginalMojo() {
    return DisplayDependencyUpdatesMojo.class;
  }

  @Override
  public Class<DisplayDependencyUpdatesBridgeMojo> getReplacingMojo() {
    return DisplayDependencyUpdatesBridgeMojo.class;
  }

}

abstract class MojoExecutionHandler<ORIGINAL_MOJO extends Mojo, REPLACING_MOJO extends Mojo> {

  @SuppressWarnings("unchecked")
  public final void beforeExecution(final Mojo mojo) {
    beforeExecution2((REPLACING_MOJO) mojo);
  }

  protected abstract void beforeExecution2(final REPLACING_MOJO mojo);

  @SuppressWarnings("unchecked")
  public final void afterExecution(final Mojo mojo) {
    afterExecution2((REPLACING_MOJO) mojo);
  }

  protected abstract void afterExecution2(final REPLACING_MOJO mojo);

  public abstract Class<ORIGINAL_MOJO> getOriginalMojo();

  public abstract Class<REPLACING_MOJO> getReplacingMojo();

}
