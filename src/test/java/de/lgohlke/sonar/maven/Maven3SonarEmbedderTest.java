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

import hudson.maven.MavenEmbedderException;
import org.apache.maven.lifecycle.LifecyclePhaseNotFoundException;
import org.apache.maven.plugin.MojoNotFoundException;
import org.codehaus.plexus.logging.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;

import static org.fest.assertions.api.Assertions.assertThat;

public class Maven3SonarEmbedderTest {
  // public static final File MAVEN_HOME = new File("/data/home/lgohlke/development/tools/apache-maven-3.0.4");
  public static final File MAVEN_HOME = new File("/home/lars/development/tools/apache-maven-3.0.4");

  @BeforeTest(alwaysRun = true)
  @BeforeMethod(alwaysRun = true)
  public void setUp() {
    System.getProperties().remove(Maven3SonarEmbedder.MavenSonarEmbedderBuilder.M2_HOME);
    System.getProperties().remove(Maven3SonarEmbedder.MavenSonarEmbedderBuilder.MAVEN_HOME_KEY);
  }

  final String goal = "versions:help";

  @Test
  public void shouldNotFailOnMissingPom() throws MavenEmbedderException {
    Maven3SonarEmbedder.configure().
    goal(goal).
    setAlternativeMavenHome(MAVEN_HOME).
    build();
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldFailOnMissingGoal() throws MavenEmbedderException {
    Maven3SonarEmbedder.configure().
    usePomFile("pom.xml").
    setAlternativeMavenHome(MAVEN_HOME).
    build();
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void shouldFailOnWrongMavenHome() throws MavenEmbedderException {
    System.setProperty(Maven3SonarEmbedder.MavenSonarEmbedderBuilder.M2_HOME, "wrong");
    System.setProperty(Maven3SonarEmbedder.MavenSonarEmbedderBuilder.MAVEN_HOME_KEY, "wrong");
    Maven3SonarEmbedder.configure().
    usePomFile("pom.xml").
    goal(goal).
    build();
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void shouldFailOnWrongMavenHomeIsNotExisting() throws MavenEmbedderException {
    System.setProperty(Maven3SonarEmbedder.MavenSonarEmbedderBuilder.M2_HOME, "wrong");
    System.setProperty(Maven3SonarEmbedder.MavenSonarEmbedderBuilder.MAVEN_HOME_KEY, "wrong");
    Maven3SonarEmbedder.configure().
    usePomFile("pom.xml").
    goal(goal).
    setAlternativeMavenHome(new File("x")).
    build();
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void shouldFailOnWrongMavenHomeIsFile() throws MavenEmbedderException {
    System.setProperty(Maven3SonarEmbedder.MavenSonarEmbedderBuilder.M2_HOME, "wrong");
    System.setProperty(Maven3SonarEmbedder.MavenSonarEmbedderBuilder.MAVEN_HOME_KEY, "wrong");
    Maven3SonarEmbedder.configure().
    usePomFile("pom.xml").
    goal(goal).
    setAlternativeMavenHome(new File("pom.xml")).
    build();
  }

  @Test
  public void shouldRun() throws MavenEmbedderException {
    Maven3SonarEmbedder.configure().
    usePomFile("pom.xml").
    goal(goal).
    setAlternativeMavenHome(MAVEN_HOME).
    build().
    run();
  }

  @Test
  public void shouldFailOnWrongGoalNoPluginFound() throws MavenEmbedderException {
    try {
      Maven3SonarEmbedder.configure().
      usePomFile("pom.xml").
      goal("not-present").
      setAlternativeMavenHome(MAVEN_HOME).
      build().
      run();
    } catch (MavenEmbedderException e) {
      assertThat(e.getCause()).isExactlyInstanceOf(LifecyclePhaseNotFoundException.class);
    }
  }

  @Test
  public void shouldFailOnWrongGoalNoPluginFound2() throws MavenEmbedderException {
    try {
      Maven3SonarEmbedder.configure().
      logLevel(Logger.LEVEL_WARN).
      usePomFile("pom.xml").
      goal("versions:helps").
      setAlternativeMavenHome(MAVEN_HOME).
      build().
      run();
    } catch (MavenEmbedderException e) {
      assertThat(e.getCause()).isExactlyInstanceOf(MojoNotFoundException.class);
    }
  }
}
