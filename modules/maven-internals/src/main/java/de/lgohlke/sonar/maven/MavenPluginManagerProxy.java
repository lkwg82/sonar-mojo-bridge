/*
 * sonar-maven-checks-maven-internals
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

import org.apache.maven.plugin.MavenPluginManager;
import org.fest.reflect.exception.ReflectionError;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MavenPluginManagerProxy<T extends MavenPluginManager> extends DynamicProxy<T> {
  private final ClassLoader classloader;

  public MavenPluginManagerProxy(final T underlying, final ClassLoader cl) {
    super(underlying);
    this.classloader = cl;
  }

  /**
   * see  MavenPluginManager#setupPluginRealm(PluginDescriptor, MavenSession, ClassLoader, List, DependencyFilter)
   */
  @Override
  public Object invoke(final Object proxy, final Method method, final Object[] args) {
    if (method.getName().equals("setupPluginRealm")) {
      args[2] = classloader;
    }
    try {
      return method.invoke(getUnderLying(), args);
    } catch (IllegalAccessException e) {
      throw new ReflectionError(e.getMessage(), e);
    } catch (InvocationTargetException e) {
      throw new ReflectionError(e.getMessage(), e);
    }
  }
}
