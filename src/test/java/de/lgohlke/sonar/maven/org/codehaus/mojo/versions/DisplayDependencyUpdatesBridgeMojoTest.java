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
package de.lgohlke.sonar.maven.org.codehaus.mojo.versions;

import org.apache.maven.project.MavenProject;
import org.testng.annotations.Test;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Mockito.mock;


/**
 * User: lgohlke
 */
public class DisplayDependencyUpdatesBridgeMojoTest {
  @Test
  public void testExecute() throws Exception {
    DisplayDependencyUpdatesBridgeMojo mojo = new DisplayDependencyUpdatesBridgeMojo();
    MavenProject mavenProject = mock(MavenProject.class);
    field("project").ofType(MavenProject.class).in(mojo).set(mavenProject);

    DisplayDependencyUpdatesBridgeMojoResultHandler handler = new DisplayDependencyUpdatesBridgeMojoResultHandler();
    mojo.injectResultHandler(handler);
    mojo.execute();

    assertThat(handler.getUpdateMap()).isNotNull();
  }
}
