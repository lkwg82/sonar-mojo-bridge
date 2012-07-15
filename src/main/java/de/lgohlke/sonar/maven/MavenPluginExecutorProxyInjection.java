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

import de.lgohlke.sonar.maven.plugin.versions.DisplayDependencyMojoProxy;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.lifecycle.LifecycleExecutor;
import org.apache.maven.lifecycle.internal.MojoExecutor;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MavenPluginManager;
import org.apache.maven.plugin.Mojo;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.sonar.batch.MavenPluginExecutor;
import org.sonar.maven3.Maven3PluginExecutor;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.fest.reflect.core.Reflection.field;

public class MavenPluginExecutorProxyInjection {

  public static void inject(final MavenPluginExecutor mavenPluginExecutor) {
    if (mavenPluginExecutor instanceof Maven3PluginExecutor) {
      decorateMaven3ExecutionProcess(mavenPluginExecutor);
    }
  }

  private static void decorateMaven3ExecutionProcess(final MavenPluginExecutor mavenPluginExecutor) {
    try {
      LifecycleExecutor lifecycleExecutor = field("lifecycleExecutor").ofType(LifecycleExecutor.class).in(mavenPluginExecutor).get();
      MavenSession mavenSession = field("mavenSession").ofType(MavenSession.class).in(mavenPluginExecutor).get();
      MojoExecutor mojoExecutor = field("mojoExecutor").ofType(MojoExecutor.class).in(lifecycleExecutor).get();
      BuildPluginManager pluginManager = field("pluginManager").ofType(BuildPluginManager.class).in(mojoExecutor).get();
      PlexusContainer container = field("container").ofType(PlexusContainer.class).in(mavenSession).get();
      MavenPluginManager mavenPluginManager = container.lookup(MavenPluginManager.class);
      mavenPluginManager = getMavenPluginManagerProxy(MavenPluginManager.class, mavenPluginManager);
      field("mavenPluginManager").ofType(MavenPluginManager.class).in(pluginManager).set(mavenPluginManager);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  @SuppressWarnings("unchecked")
  public static <T extends MavenPluginManager> T getMavenPluginManagerProxy(final Class<T> intf,
      final T obj) {
    return (T) Proxy.newProxyInstance(obj.getClass().getClassLoader(), new Class<?>[] {intf}, new MavenPluginManagerProxy<T>(obj));
  }

}

class MavenPluginManagerProxy<T extends MavenPluginManager> extends DynamicProxy<T> {

  public MavenPluginManagerProxy(final T underlying) {
    super(underlying);
  }

  @Override
  public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
    Object result = method.invoke(getUnderLying(), args);
    if (method.getName().equals("getConfiguredMojo")) {

      final ClassLoader classLoader = result.getClass().getClassLoader();
      {
        ClassRealm x = (ClassRealm) classLoader;
        x.importFrom(getClass().getClassLoader(), "de.lgohlke");
        System.out.println(x + "");
      }

      Class<?> clazz = classLoader.loadClass(DisplayDependencyMojoProxy.class.getCanonicalName());
      // final HelpMojoProxy<Mojo> proxymojo = new HelpMojoProxy<Mojo>();
      DisplayDependencyMojoProxy<Mojo> proxymojo = (DisplayDependencyMojoProxy<Mojo>) clazz.newInstance();
      // new DisplayDependencyMojoProxy<Mojo>();
      proxymojo.setUnderlying((Mojo) result);
      result = Proxy.newProxyInstance(result.getClass().getClassLoader(), new Class<?>[] {Mojo.class}, proxymojo);
    }

    return result;
  }
}
