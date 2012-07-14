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
import org.apache.maven.lifecycle.internal.MojoExecutor;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MavenPluginManager;
import org.apache.maven.plugin.Mojo;
import org.codehaus.plexus.PlexusContainer;
import org.sonar.batch.MavenPluginExecutor;
import org.sonar.maven3.Maven3PluginExecutor;

import java.lang.reflect.InvocationHandler;
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
      LifecycleExecutor lifecycleExecutor = field("lifecycleExecutor").
          ofType(LifecycleExecutor.class).
          in(mavenPluginExecutor).get();

      MavenSession mavenSession = field("mavenSession").
          ofType(MavenSession.class).
          in(mavenPluginExecutor).get();

      MojoExecutor mojoExecutor = field("mojoExecutor").ofType(MojoExecutor.class).in(lifecycleExecutor).get();
      BuildPluginManager pluginManager = field("pluginManager").ofType(BuildPluginManager.class).in(mojoExecutor).get();
      PlexusContainer container = field("container").ofType(PlexusContainer.class).in(mavenSession).get();

      MavenPluginManager mavenPluginManager = container.lookup(MavenPluginManager.class);
      mavenPluginManager = getMavenPluginManagerProxy(MavenPluginManager.class, mavenPluginManager);
      // container.release(mavenPluginManager);
      // container.addComponent(mavenPluginManager, MavenPluginManager.class.getName());
      // mavenPluginManager = container.lookup(MavenPluginManager.class);
      // System.out.println(pluginManager);
      field("mavenPluginManager").ofType(MavenPluginManager.class).in(pluginManager).set(mavenPluginManager);
      // System.out.println(mavenPluginManager);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @SuppressWarnings("unchecked")
  public static <T extends MavenPluginManager> T getMavenPluginManagerProxy(final Class<T> intf,
      final T obj) {
    return (T) Proxy.newProxyInstance(obj.getClass().getClassLoader(), new Class<?>[] {intf}, new MavenPluginManagerProxy<T>(obj));
  }

}

class MavenPluginManagerProxy<T extends MavenPluginManager> extends ProxyAbstract<T> {

  public MavenPluginManagerProxy(final T underlying) {
    super(underlying);
  }

  @Override
  public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {

    Object result = method.invoke(getUnderLying(), args);
    if (method.getName().equals("getConfiguredMojo")) {
      final HelpMojoProxy<Mojo> proxymojo = new HelpMojoProxy<Mojo>((Mojo) result);
      result = Proxy.newProxyInstance(result.getClass().getClassLoader(), new Class<?>[] {Mojo.class}, proxymojo);
    }

    return result;
  }
}

class HelpMojoProxy<T extends Mojo> extends MyMojoProxy<T> {

  public HelpMojoProxy(final T underlying) {
    super(underlying);
  }

  @Override
  public void beforeExecution(final Mojo mojo) {
    System.out.println("before " + mojo);
  }

  @Override
  public void afterExecution(final Mojo mojo) {
    System.out.println("after " + mojo);
  }

  @Override
  public boolean useAlternativeExecute() {
    return true;
  }

}

abstract class ProxyAbstract<T> implements InvocationHandler {
  private T underlying;

  public ProxyAbstract(final T underlying) {
    this.underlying = underlying;
  }

  public T getUnderLying() {
    return underlying;
  }

  public void setUnderlying(final T underlying) {
    this.underlying = underlying;
  }
}

abstract class MyMojoProxy<T extends Mojo> extends ProxyAbstract<T> {

  public MyMojoProxy(final T underlying) {
    super(underlying);
  }

  public abstract void beforeExecution(T mojo);

  public abstract void afterExecution(T mojo);

  public abstract boolean useAlternativeExecute();

  public void alternativeExecute(final T mojo) {
    // can be overriden
  }

  @Override
  public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {

    Object result = null;
    if (method.getName().equals("execute") && args == null) {
      beforeExecution(getUnderLying());
      if (useAlternativeExecute()) {
        alternativeExecute(getUnderLying());
      } else {
        result = method.invoke(getUnderLying(), args);
      }
      afterExecution(getUnderLying());
    } else {
      result = method.invoke(getUnderLying(), args);
    }
    return result;
  }

}
