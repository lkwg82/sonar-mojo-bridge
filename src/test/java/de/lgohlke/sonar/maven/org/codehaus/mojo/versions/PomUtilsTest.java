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

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.model.Dependency;
import org.testng.annotations.Test;

import java.io.File;

import static org.fest.assertions.api.Assertions.assertThat;

public class PomUtilsTest {

  @Test
  public void testFindLineForDependencyUpdate() throws Exception {
    String pom = "src/test/resources/pom_missing_maven_version.xml";
    String source = FileUtils.readFileToString(new File(pom));

    Dependency dependency = new Dependency();
    dependency.setGroupId("org.apache.maven.plugins");
    dependency.setArtifactId("maven-surefire-plugin");
    dependency.setVersion("2.10");

    ArtifactVersion artifactVersion = new DefaultArtifactVersion("1.0");

    ArtifactUpdate artifactUpdate = new ArtifactUpdate(dependency, artifactVersion);

    assertThat(PomUtils.getLine(source, artifactUpdate,PomUtils.TYPE.plugin)).isEqualTo(37);
  }


}
