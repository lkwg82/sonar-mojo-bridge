/*
 * sonar-mojo-bridge-maven-internals
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

import org.sonar.api.batch.maven.MavenPluginHandler;
import org.testng.annotations.Test;
import static org.fest.assertions.api.Assertions.assertThat;


/**
 * Created with IntelliJ IDEA.
 * User: lgohlke
 */
public class MavenPluginHandlerFactoryTest {
  @Test
  public void testCreateHandler() throws Exception {
    MavenPluginHandler handler = MavenPluginHandlerFactory.createHandler("group:artifact:version:goal");

    assertThat(handler.getGroupId()).isEqualTo("group");
    assertThat(handler.getArtifactId()).isEqualTo("artifact");
    assertThat(handler.getVersion()).isEqualTo("version");
    assertThat(handler.getGoals()).hasSize(1);
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void shouldFailOnEmptyGroupArtifactGoal() throws Exception {
    MavenPluginHandlerFactory.createHandler("");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void shouldFailOnToShortGroupArtifactGoal() throws Exception {
    MavenPluginHandlerFactory.createHandler("a:b:c");
  }
}
