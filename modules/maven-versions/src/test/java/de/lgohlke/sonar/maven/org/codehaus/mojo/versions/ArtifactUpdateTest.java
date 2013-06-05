/*
 * sonar-mojo-bridge-maven-versions
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

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.model.Dependency;
import org.testng.annotations.Test;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * User: lars
 */
public class ArtifactUpdateTest {
  @Test
  public void test() {
    Dependency dependency = new Dependency();
    dependency.setGroupId("g");
    dependency.setArtifactId("a");
    dependency.setVersion("2.0");
    ArtifactUpdate artifactUpdate = new ArtifactUpdate(dependency, new DefaultArtifactVersion("2.1.0"));

    assertThat(artifactUpdate.toString()).isEqualTo("g:a:2.0 has newer version (2.1.0) available");
  }
}
