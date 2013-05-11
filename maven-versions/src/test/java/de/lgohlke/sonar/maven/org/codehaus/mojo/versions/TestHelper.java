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

import de.lgohlke.sonar.PomSourceImporter;
import org.apache.maven.project.MavenProject;

import java.io.File;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * User: lars
 */
public class TestHelper {
  static MavenProject getMavenProject() {
    MavenProject mavenProject = mock(MavenProject.class);
    when(mavenProject.getFile()).thenReturn(new File("."));
    return mavenProject;
  }

  static PomSourceImporter getPomSourceImporter() {
    PomSourceImporter pomSourceImporter = mock(PomSourceImporter.class);
    when(pomSourceImporter.getPomFile()).thenReturn(new org.sonar.api.resources.File("", "pom.xml"));
    when(pomSourceImporter.getSourceOfPom()).thenReturn("");
    return pomSourceImporter;
  }
}
