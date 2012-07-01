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
package de.lgohlke.MavenVersion;

import hudson.maven.MavenEmbedderException;
import org.apache.maven.lifecycle.LifecyclePhaseNotFoundException;
import org.apache.maven.plugin.MojoNotFoundException;
import org.fest.assertions.Assertions;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class MavenEmbedderTest {
  private static final String MAVEN_HOME_KEY = "maven.home";
  private static final String M2_HOME_KEY = "M2_HOME";
  // private static final File MAVEN_HOME = new File("/data/home/lgohlke/development/tools/apache-maven-3.0.4");
  private static final File MAVEN_HOME = new File("/home/lars/development/tools/apache-maven-3.0.4");

  @Test
  public void testRun() throws Exception {

    // final MojoExecutionHandler mojoExectionHandler = new MojoExecutionHandler() {
    //
    // @Override
    // public void beforeExecution(final Mojo mojo) {
    // assertThat(mojo).isNotNull();
    // }
    //
    // @Override
    // public void afterExecution(final Mojo mojo) {
    // assertThat(mojo).isNotNull();
    // }
    //
    // @Override
    // public Map<String, Class<? extends Mojo>> getMojoMapping() {
    // return new HashMap<String, Class<? extends Mojo>>() {
    // private static final long serialVersionUID = -2694116074705405757L;
    // {
    // put(HelpMojo.class.getCanonicalName(), MyHelpMojo.class);
    // put(DisplayDependencyUpdatesMojo.class.getCanonicalName(), MyDisplay.class);
    // }
    // };
    // }
    // };

    MavenSonarEmbedder.configure().
        usePomFile("pom.xml").
        goal("versions:help").
        setAlternativeMavenHome(MAVEN_HOME).
        setMojoExecutionHandler(mock(MojoExecutionHandler.class)).
        build().run();
  }

  final String goal = "versions:display-dependency-updates";

  @Test(expected = NullPointerException.class)
  public void shouldFailOnMissingPom() throws MavenEmbedderException {
    MavenSonarEmbedder.configure().
        goal(goal).
        setAlternativeMavenHome(MAVEN_HOME).
        setMojoExecutionHandler(Mockito.mock(MojoExecutionHandler.class)).
        build();
  }

  @Test(expected = NullPointerException.class)
  public void shouldFailOnMissingGoal() throws MavenEmbedderException {
    MavenSonarEmbedder.configure().
        usePomFile("pom.xml").
        setAlternativeMavenHome(MAVEN_HOME).
        setMojoExecutionHandler(Mockito.mock(MojoExecutionHandler.class)).
        build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldFailOnWrongMavenHome() throws MavenEmbedderException {
    System.setProperty(M2_HOME_KEY, "/x");
    MavenSonarEmbedder.configure().
        usePomFile("pom.xml").
        goal(goal).
        // setAlternativeMavenHome(MAVEN_HOME).
        setMojoExecutionHandler(Mockito.mock(MojoExecutionHandler.class)).
        build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldFailOnWrongMavenHomeIsNotExisting() throws MavenEmbedderException {
    System.setProperty(M2_HOME_KEY, "wrong");
    System.setProperty(MAVEN_HOME_KEY, "wrong");
    MavenSonarEmbedder.configure().
        usePomFile("pom.xml").
        goal(goal).
        setAlternativeMavenHome(new File("x")).
        setMojoExecutionHandler(Mockito.mock(MojoExecutionHandler.class)).
        build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldFailOnWrongMavenHomeIsFile() throws MavenEmbedderException {
    System.setProperty(M2_HOME_KEY, "wrong");
    System.setProperty(MAVEN_HOME_KEY, "wrong");
    MavenSonarEmbedder.configure().
        usePomFile("pom.xml").
        goal(goal).
        setAlternativeMavenHome(new File("pom.xml")).
        setMojoExecutionHandler(Mockito.mock(MojoExecutionHandler.class)).
        build();
  }

  @Test
  public void shouldFailOnWrongGoalNoPluginFound() throws MavenEmbedderException {
    try {
      MavenSonarEmbedder.configure().
          usePomFile("pom.xml").
          goal("not-present").
          setAlternativeMavenHome(MAVEN_HOME).
          setMojoExecutionHandler(Mockito.mock(MojoExecutionHandler.class)).
          build().
          run();
    } catch (MavenEmbedderException e) {
      assertThat(e.getCause()).isExactlyInstanceOf(LifecyclePhaseNotFoundException.class);
    }
  }

  @Test
  public void shouldFailOnWrongGoalNoPluginFound2() throws MavenEmbedderException {
    try {
      MavenSonarEmbedder.configure().
          usePomFile("pom.xml").
          goal("versions:helps").
          setAlternativeMavenHome(MAVEN_HOME).
          setMojoExecutionHandler(Mockito.mock(MojoExecutionHandler.class)).
          build().
          run();
    } catch (MavenEmbedderException e) {
      Assertions.assertThat(e.getCause()).isExactlyInstanceOf(MojoNotFoundException.class);
    }
  }

}
