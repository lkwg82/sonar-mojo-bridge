/*
 * sonar-maven-checks-maven-internals
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

import hudson.maven.MavenEmbedder;
import hudson.maven.MavenEmbedderException;
import hudson.maven.MavenRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.cli.MavenLoggerManager;
import org.apache.maven.execution.MavenExecutionResult;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.*;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class Maven3SonarEmbedder {
  private final MavenEmbedder embedder;
  private final MavenRequest mavenRequest;

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
    public static final String M2_HOME = "M2_HOME";
    public static final String MAVEN_HOME_KEY = "maven.home";
    public static final int MIN_LOG_LEVEL = 0;
    public static final int MAX_LOG_LEVEL = 5;
    private String pom = "pom.xml"; // default
    private String mavenGoal = null;
    private File mavenHome = null;
    private int mavenLogLevel = org.codehaus.plexus.logging.Logger.LEVEL_ERROR;
    private boolean mavenShowErrors;

    public MavenSonarEmbedderBuilder usePomFile(final String pomFile) {
      checkNotNull(pomFile);
      this.pom = pomFile;
      return this;
    }

    public MavenSonarEmbedderBuilder goal(final String goal) {
      checkNotNull(goal);
      this.mavenGoal = goal;
      return this;
    }

    /**
     * <pre>
     * int LEVEL_DEBUG = 0;
     *
     * int LEVEL_INFO = 1;
     *
     * int LEVEL_WARN = 2;
     *
     * int LEVEL_ERROR = 3;
     *
     * int LEVEL_FATAL = 4;
     *
     * int LEVEL_DISABLED = 5;
     * </pre>
     */
    public MavenSonarEmbedderBuilder logLevel(final int level) {
      checkArgument((level >= MIN_LOG_LEVEL) && (level <= MAX_LOG_LEVEL));
      this.mavenLogLevel = level;
      return this;
    }

    /**
     * could be called multiple times
     */
    public MavenSonarEmbedderBuilder setAlternativeMavenHome(final File mavenHome) {
      checkNotNull(mavenHome);
      if ((this.mavenHome == null) && mavenHome.isDirectory()) {
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
        } else if (envMap.containsKey(M2_HOME)) {
          mavenHome = new File(envMap.get(M2_HOME));
        } else {
          mavenHome = getMavenHomeFromRunningInstance();
        }

        checkNotNull(mavenHome, "we did not find the maven directory");
        checkArgument(mavenHome.isDirectory(),
            "maveHome is " + mavenHome + ", but a directory is needed");
      }
    }

    private File getMavenHomeFromRunningInstance() {
      final String currentProgramm = System.getenv("_");
      if (currentProgramm != null) {
        File mvnBinary = new File(currentProgramm);
        if (mvnBinary.exists()) {
          return mvnBinary.getParentFile().getParentFile();
        }
      }
      return null;
    }

    public MavenSonarEmbedderBuilder showErrors(final boolean showErrors) {
      this.mavenShowErrors = showErrors;
      return this;
    }

    public Maven3SonarEmbedder build() throws MavenEmbedderException {
      checkNotNull(pom, "missing pom");

      checkNotNull(mavenGoal, "missing mavenGoal");
      checkState(mavenGoal.length() > 0, "mavenGoal is empty");

      MavenRequest mavenRequest = new MavenRequest();
      mavenRequest.setPom(pom);
      mavenRequest.setShowErrors(mavenShowErrors);
      mavenRequest.setGoals(Arrays.asList(mavenGoal));
      mavenRequest.setLoggingLevel(mavenLogLevel);
      mavenRequest.setMavenLoggerManager(new MavenLoggerManager(new PlexusSlf4JLogger(Maven3SonarEmbedder.log)));
      detectMavenHomeIfNull();

      try {
        final File m2File = new File(mavenHome.getCanonicalPath() + "/bin/m2.conf");
        checkArgument(m2File.exists(), "not found bin/m2.conf in " + mavenHome);
      } catch (IOException e1) {
        throw new MavenEmbedderException(e1);
      } catch (IllegalArgumentException e2) {
        throw new MavenEmbedderException(e2);
      }

      final MavenEmbedder embedder = new MavenEmbedder(mavenHome, mavenRequest);
      return new Maven3SonarEmbedder(embedder, mavenRequest);
    }
  }
}
