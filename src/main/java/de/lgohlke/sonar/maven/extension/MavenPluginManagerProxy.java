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

import org.apache.maven.plugin.MavenPluginManager;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MavenPluginManagerProxy<T extends MavenPluginManager> extends DynamicProxy<T> {

  private final ClassLoader classloader;

  public MavenPluginManagerProxy(final T underlying, final ClassLoader cl) {
    super(underlying);
    this.classloader = cl;
  }

  @Override
  public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
    if (method.getName().equals("setupPluginRealm")) {
      args[2] = classloader;
      final List<String> imports = new ArrayList<String>();
      imports.add("de.lgohlke");
      args[3] = imports;
    }
    return method.invoke(getUnderLying(), args);
  }
}