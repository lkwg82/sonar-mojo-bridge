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

import com.google.common.base.Preconditions;
import hudson.maven.MavenEmbedder;
import hudson.maven.MavenEmbedderException;
import hudson.maven.MavenRequest;
import org.apache.maven.InternalErrorException;
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

  public MavenSonarEmbedder(final MavenEmbedder embedder, final MavenRequest mavenRequest) {
    this.embedder = embedder;
    this.mavenRequest = mavenRequest;
  }

  public void run() throws MavenEmbedderException {
    try {
      MavenExecutionResult result = embedder.execute(mavenRequest);
      if (result.hasExceptions()) {
        final Throwable firstException = result.getExceptions().get(0);
        if (firstException instanceof InternalErrorException && firstException.getCause() != null && firstException.getCause() instanceof StopMavenExectionException) {
          // everything ok, this is the workaround
        } else {
          throw new MavenEmbedderException(firstException);
        }
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
    MojoExecutionHandler mojoExectionHandler;

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

    public MavenSonarEmbedderBuilder setMojoExecutionHandler(final MojoExecutionHandler<?, ?> mojoExectionHandler) {
      Preconditions.checkNotNull(mojoExectionHandler);
      this.mojoExectionHandler = mojoExectionHandler;
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
        Preconditions.checkArgument(mavenHome.isDirectory());
      }
    }

    public MavenSonarEmbedder build() throws MavenEmbedderException {

      Preconditions.checkNotNull(pom, "missing pom");
      Preconditions.checkNotNull(goal, "missing goal");
      Preconditions.checkNotNull(mojoExectionHandler, "missing mojoExectionHandler");

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

      final ExecutionListenerImpl executionListener = new ExecutionListenerImpl(mojoExectionHandler);
      mavenRequest.setExecutionListener(executionListener);
      final MavenEmbedder embedder = new MavenEmbedder(mavenHome, mavenRequest);
      // try {
      // System.out.println("this issa " + embedder.lookup(MavenLoggerManager.class));
      // } catch (ComponentLookupException e) {
      // // TODO Auto-generated catch block
      // e.printStackTrace();
      // }
      executionListener.setEmbedder(embedder);
      return new MavenSonarEmbedder(embedder, mavenRequest);
    }
  }
}
