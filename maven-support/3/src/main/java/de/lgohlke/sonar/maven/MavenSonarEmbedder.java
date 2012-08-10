/*
 * Sonar maven checks plugin (maven3 support)
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

import com.google.common.base.Preconditions;
import de.lgohlke.sonar.maven.extension.MyPlexusLogger;
import hudson.maven.MavenEmbedder;
import hudson.maven.MavenEmbedderException;
import hudson.maven.MavenRequest;
import org.apache.maven.cli.MavenLoggerManager;
import org.apache.maven.execution.MavenExecutionResult;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MavenSonarEmbedder {
  private final static Logger logger = LoggerFactory.getLogger(MavenSonarEmbedder.class);
  private final MavenRequest mavenRequest;
  private final MavenEmbedder embedder;

  private MavenSonarEmbedder(final MavenEmbedder embedder, final MavenRequest mavenRequest) {
    this.embedder = embedder;
    this.mavenRequest = mavenRequest;
  }

  public void run() throws MavenEmbedderException {
    try {
      MavenExecutionResult result = embedder.execute(mavenRequest);
      if (result.hasExceptions()) {
        final Throwable firstException = result.getExceptions().get(0);
        throw new MavenEmbedderException(firstException);
      }
    } catch (ComponentLookupException e) {
      throw new MavenEmbedderException(e);
    }
  }

  public static MavenSonarEmbedderBuilder configure() {
    return new MavenSonarEmbedderBuilder();
  }

  public static class MavenSonarEmbedderBuilder {
    private String pom;
    private String goal;
    private File mavenHome;

    public MavenSonarEmbedderBuilder usePomFile(final String pomFile) {
      Preconditions.checkNotNull(pomFile);
      this.pom = pomFile;
      return this;
    }

    public MavenSonarEmbedderBuilder goal(final String goal) {
      Preconditions.checkNotNull(goal);
      this.goal = goal;
      return this;
    }

    /**
    * could be called multiple times
    * @param mavenHome
    * @return
    */
    public MavenSonarEmbedderBuilder setAlternativeMavenHome(final File mavenHome) {
      Preconditions.checkNotNull(mavenHome);
      if (this.mavenHome == null && mavenHome.isDirectory()) {
        this.mavenHome = mavenHome;
      }
      return this;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void detectMavenHomeIfNull() {
      if (mavenHome == null) {
        Map<String, String> envMap = new HashMap<String, String>(System.getenv());
        envMap.putAll((Map) System.getProperties());
        if (envMap.containsKey("maven.home")) {
          mavenHome = new File(envMap.get("maven.home"));
        }
        else if (envMap.containsKey("M2_HOME")) {
          mavenHome = new File(envMap.get("M2_HOME"));
        } else {
          final String currentProgramm = System.getenv("_");
          if (currentProgramm != null) {
            File mvnBinary = new File(currentProgramm);
            if (mvnBinary != null) {
              if (mvnBinary.exists())
              {
                mavenHome = mvnBinary.getParentFile().getParentFile();
              }
            }
          }
        }

        Preconditions.checkNotNull(mavenHome, "we did not find the maven directory");
        Preconditions.checkArgument(mavenHome.isDirectory(), "maveHome is " + mavenHome + ", but a directory is needed");
      }
    }

    public MavenSonarEmbedder build() throws MavenEmbedderException {

      Preconditions.checkNotNull(pom, "missing pom");

      Preconditions.checkNotNull(goal, "missing goal");
      Preconditions.checkState(goal.length() > 0, "goal is empty");

      MavenRequest mavenRequest = new MavenRequest();
      mavenRequest.setPom(pom);
      mavenRequest.setShowErrors(true);
      mavenRequest.setGoals(Arrays.asList(goal));
      mavenRequest.setLoggingLevel(1);
      mavenRequest.setMavenLoggerManager(new MavenLoggerManager(new MyPlexusLogger(logger)));
      detectMavenHomeIfNull();

      try {
        final File m2File = new File(mavenHome.getCanonicalPath() + "/bin/m2.conf");
        Preconditions.checkArgument(m2File.exists(), "not found bin/m2.conf in " + mavenHome);
      } catch (IOException e1) {
        throw new MavenEmbedderException(e1);
      }

      final MavenEmbedder embedder = new MavenEmbedder(mavenHome, mavenRequest);
      return new MavenSonarEmbedder(embedder, mavenRequest);
    }
  }
}
