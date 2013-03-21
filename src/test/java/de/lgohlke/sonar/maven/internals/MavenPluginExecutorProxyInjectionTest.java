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

import org.sonar.batch.scan.maven.MavenPluginExecutor;
import org.sonar.maven3.Maven3PluginExecutor;
import org.testng.annotations.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * User: lars
 */
public class MavenPluginExecutorProxyInjectionTest {
  @Test
  public void testCheckIfIsMaven3Negative() throws Exception {
    MavenPluginExecutor mavenPluginExecutor = mock(MavenPluginExecutor.class);
    assertThat(MavenPluginExecutorProxyInjection.checkIfIsMaven3(mavenPluginExecutor)).isFalse();
  }

  @Test
  public void testCheckIfIsMaven3Positiv() throws Exception {
    MavenPluginExecutor mavenPluginExecutor = mock(Maven3PluginExecutor.class);
    assertThat(MavenPluginExecutorProxyInjection.checkIfIsMaven3(mavenPluginExecutor)).isTrue();
  }
}
