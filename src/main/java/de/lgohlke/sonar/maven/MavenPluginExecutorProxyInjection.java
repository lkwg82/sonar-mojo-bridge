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

import de.lgohlke.sonar.maven.plugin.versions.BridgeMojoMapper;
import org.sonar.batch.MavenPluginExecutor;
import org.sonar.maven3.Maven3PluginExecutor;

public class MavenPluginExecutorProxyInjection {

  public static void inject(final MavenPluginExecutor mavenPluginExecutor, final ClassLoader classLoader, final BridgeMojoMapper handler) {
    try {
      if (mavenPluginExecutor instanceof Maven3PluginExecutor) {
        Maven3ExecutionProcess.decorate(mavenPluginExecutor, classLoader, handler);
      }
    } catch (NoClassDefFoundError e) {

    }
  }

  public static boolean checkIfIsMaven3(final MavenPluginExecutor mavenPluginExecutor) {
    try {
      if (mavenPluginExecutor instanceof Maven3PluginExecutor) {
        return true;
      } else {
        return false;
      }
    } catch (NoClassDefFoundError e) {
      return false;
    }
  }
}
