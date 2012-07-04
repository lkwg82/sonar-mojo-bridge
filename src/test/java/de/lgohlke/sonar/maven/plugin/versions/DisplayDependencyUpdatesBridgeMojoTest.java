/*
 * Sonar maven checks plugin
 * Copyright (C) 2012 ${owner}
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
package de.lgohlke.sonar.maven.plugin.versions;

import de.lgohlke.sonar.maven.plugin.versions.DisplayDependencyUpdatesBridgeMojo;

import de.lgohlke.MavenVersion.MavenEmbedderTest;
import de.lgohlke.sonar.maven.MavenSonarEmbedder;
import de.lgohlke.sonar.maven.MojoExecutionHandler;
import hudson.maven.MavenEmbedderException;
import org.codehaus.mojo.versions.DisplayDependencyUpdatesMojo;
import org.testng.annotations.Test;

import static org.fest.assertions.Assertions.assertThat;

public class DisplayDependencyUpdatesBridgeMojoTest {

  @Test
  public void test() throws MavenEmbedderException {

    final MojoExecutionHandler<DisplayDependencyUpdatesMojo, DisplayDependencyUpdatesBridgeMojo> mojoExectionHandler = new MojoExecutionHandler<DisplayDependencyUpdatesMojo, DisplayDependencyUpdatesBridgeMojo>() {

      @Override
      protected void beforeExecution2(final DisplayDependencyUpdatesBridgeMojo mojo) {
        assertThat(mojo).isNotNull();
        assertThat(mojo.getUpdateMap()).isEmpty();
      }

      @Override
      protected void afterExecution2(final DisplayDependencyUpdatesBridgeMojo mojo) {
        assertThat(mojo).isNotNull();
        assertThat(mojo.getUpdateMap()).isNotEmpty();
      }

      @Override
      public Class<DisplayDependencyUpdatesMojo> getOriginalMojo() {
        return DisplayDependencyUpdatesMojo.class;
      }

      @Override
      public Class<DisplayDependencyUpdatesBridgeMojo> getReplacingMojo() {
        return DisplayDependencyUpdatesBridgeMojo.class;
      }
    };

    MavenSonarEmbedder.configure().
        usePomFile("src/test/resources/pom-sonar-squid.xml").
        setAlternativeMavenHome(MavenEmbedderTest.MAVEN_HOME).
        setMojoExecutionHandler(mojoExectionHandler).
        build().run();

  }
}
