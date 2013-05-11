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
import org.sonar.batch.scan.maven.MavenPluginExecutor;
import org.sonar.maven3.Maven3PluginExecutor;


public final class MavenPluginExecutorProxyInjection {
  private MavenPluginExecutorProxyInjection() {
  }

  public static void inject(final MavenPluginExecutor mavenPluginExecutor, final ClassLoader classLoader, final BridgeMojoMapper handler) {
    if (checkIfIsMaven3(mavenPluginExecutor)) {
      Maven3ExecutionProcess.decorate(mavenPluginExecutor, classLoader, handler);
    }
  }

  public static boolean checkIfIsMaven3(final MavenPluginExecutor mavenPluginExecutor) {
    try {
      return mavenPluginExecutor instanceof Maven3PluginExecutor;
    } catch (NoClassDefFoundError e) {
      return false; // ok, this happens when maven 2 is used
    }
  }
}
