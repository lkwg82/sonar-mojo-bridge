/*
 * sonar-mojo-bridge-maven-plugins
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
package de.lgohlke.sonar.maven.enforcer;

import org.apache.maven.model.Plugin;
import org.sonar.api.batch.maven.MavenPlugin;
import org.sonar.api.batch.maven.MavenPluginHandler;
import org.testng.annotations.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * User: lars
 */
public class EnforceMavenPluginHandlerTest {
  @Test
  public void testPluginConfigure() {
    EnforceMavenPluginHandler enforceMavenPluginHandler = new EnforceMavenPluginHandler(mock(MavenPluginHandler.class));
    enforceMavenPluginHandler.setParameter("a", "b").setParameter("c", "d");
    MavenPlugin mavenPlugin = new MavenPlugin(new Plugin());
    enforceMavenPluginHandler.configure(null, mavenPlugin);

    assertThat(mavenPlugin.getParameter("a")).isEqualTo("b");
    assertThat(mavenPlugin.getParameter("c")).isEqualTo("d");
  }
}
