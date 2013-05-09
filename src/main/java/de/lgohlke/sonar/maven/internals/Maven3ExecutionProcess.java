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
package de.lgohlke.sonar.maven.internals;

import de.lgohlke.sonar.maven.BridgeMojoMapper;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MavenPluginManager;
import org.codehaus.plexus.PlexusContainer;
import org.sonar.batch.scan.maven.MavenPluginExecutor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import static org.fest.reflect.core.Reflection.field;


public final class Maven3ExecutionProcess {
  private Maven3ExecutionProcess() {
  }

  public static void decorate(final MavenPluginExecutor mavenPluginExecutor, final ClassLoader classLoader, final BridgeMojoMapper handler) {
    try {
      PlexusContainer container = getOrCreatePlexusContainerProxyInMavenSession(mavenPluginExecutor);
      MavenPluginManager mavenPluginManager = container.lookup(MavenPluginManager.class);
      BuildPluginManager pluginManager = container.lookup(BuildPluginManager.class);
      PlexusContainer plexusContainerProxy = getOrCreatePlexusContainerProxy(container, mavenPluginManager);
      addMojoInjection(plexusContainerProxy, handler);
      mavenPluginManager = getMavenPluginManagerProxy(MavenPluginManager.class, mavenPluginManager, classLoader);
      field("mavenPluginManager").ofType(MavenPluginManager.class).in(pluginManager).set(mavenPluginManager);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private static PlexusContainer getOrCreatePlexusContainerProxyInMavenSession(final MavenPluginExecutor mavenPluginExecutor) {
    PlexusContainer oldContainer = field("mavenSession.container").ofType(PlexusContainer.class).in(mavenPluginExecutor).get();
    if (oldContainer instanceof Proxy) {
      return oldContainer;
    } else {
      PlexusContainer plexusContainerProxy = getPlexusContainerProxy(PlexusContainer.class, oldContainer);
      field("mavenSession.container").ofType(PlexusContainer.class).in(mavenPluginExecutor).set(plexusContainerProxy);
      return plexusContainerProxy;
    }
  }

  private static PlexusContainer getOrCreatePlexusContainerProxy(final PlexusContainer container, final MavenPluginManager mavenPluginManager) {
    PlexusContainer oldContainer = field("container").ofType(PlexusContainer.class).in(mavenPluginManager).get();
    if (oldContainer instanceof Proxy) {
      return oldContainer;
    } else {
      PlexusContainer plexusContainerProxy = getPlexusContainerProxy(PlexusContainer.class, container);
      field("container").ofType(PlexusContainer.class).in(mavenPluginManager).set(plexusContainerProxy);
      return plexusContainerProxy;
    }
  }

  private static void addMojoInjection(final PlexusContainer plexusContainerProxy, final BridgeMojoMapper handler) {
    InvocationHandler invocationHandler = Proxy.getInvocationHandler(plexusContainerProxy);
    PlexusContainerProxy dynamicProxy = (PlexusContainerProxy) invocationHandler;
    dynamicProxy.addInjection(new MojoInjection(handler));
  }

  private static <T extends MavenPluginManager> T getMavenPluginManagerProxy(final Class<T> intf, final T obj, final ClassLoader cl) {
    return newInstance(obj, intf, new MavenPluginManagerProxy<T>(obj, cl));
  }

  private static <T extends PlexusContainer> T getPlexusContainerProxy(final Class<T> intf, final T obj) {
    return newInstance(obj, intf, new PlexusContainerProxy<T>(obj));
  }

  @SuppressWarnings("unchecked")
  private static <T> T newInstance(final Object obj, final Class<T> interfaze, final DynamicProxy<?> proxy) {
    return (T) Proxy.newProxyInstance(obj.getClass().getClassLoader(), new Class<?>[]{interfaze}, proxy);
  }
}
