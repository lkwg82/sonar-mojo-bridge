/*
 * sonar-mojo-bridge-testing
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
package de.lgohlke.sonar;

import com.google.common.base.Preconditions;
import hudson.maven.MavenEmbedderException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import static org.fest.assertions.api.Assertions.fail;


@RequiredArgsConstructor
@Slf4j
public final class SonarExecutor implements Cloneable {
  private final String jdbcDriver;
  private final String jdbcUrl;
  private boolean skipTests;
  private boolean skipDesign;
  private boolean skipDynamicAnalysis;
  private boolean activateMavenDebug;
  private boolean showMavenErrorWhileAnalysis;
  private boolean showMavenOutputWhileAnalysis;
  private File pomXML = new File("pom.xml");

  public SonarExecutor skipTests() {
    return skipTests(true);
  }

  private SonarExecutor skipTests(final boolean skipTests) {
    this.skipTests = skipTests;
    return this;
  }

  public SonarExecutor skipDesign() {
    return skipDesign(true);
  }

  private SonarExecutor skipDesign(final boolean skipDesign) {
    this.skipDesign = skipDesign;
    return this;
  }

  public SonarExecutor skipDynamicAnalysis() {
    return skipDynamicAnalysis(true);
  }

  private SonarExecutor skipDynamicAnalysis(final boolean skipDynamicAnalysis) {
    this.skipDynamicAnalysis = skipDynamicAnalysis;
    return this;
  }

  public SonarExecutor usePom(final File pom) {
    Preconditions.checkArgument(pom.isFile());
    this.pomXML = pom;
    return this;
  }

  public SonarExecutor activateMavenDebug() {
    return activateMavenDebug(true);
  }

  private SonarExecutor activateMavenDebug(final boolean activateMavenDebug) {
    this.activateMavenDebug = activateMavenDebug;
    return this;
  }

  public SonarExecutor showMavenErrorWhileAnalysis() {
    return showMavenErrorWhileAnalysis(true);
  }

  private SonarExecutor showMavenErrorWhileAnalysis(final boolean showMavenErrorWhileAnalysis) {
    this.showMavenErrorWhileAnalysis = showMavenErrorWhileAnalysis;
    return this;
  }

  public SonarExecutor showMavenOutputWhileAnalysis() {
    return showMavenOutputWhileAnalysis(true);
  }

  private SonarExecutor showMavenOutputWhileAnalysis(final boolean showMavenOutputWhileAnalysis) {
    this.showMavenOutputWhileAnalysis = showMavenOutputWhileAnalysis;
    return this;
  }

  @Override
  public SonarExecutor clone() throws CloneNotSupportedException {
    return ((SonarExecutor) super.clone()).activateMavenDebug(activateMavenDebug)
      .showMavenErrorWhileAnalysis(showMavenErrorWhileAnalysis)
      .showMavenOutputWhileAnalysis(showMavenOutputWhileAnalysis)
      .skipDesign(skipDesign)
      .skipDynamicAnalysis(skipDynamicAnalysis)
      .skipTests(skipTests);
  }

  public void execute() throws MavenEmbedderException {
    final String command = configureExecutionCommand();

    try {
      log.info("calling : {}", command);

      Process proc = Runtime.getRuntime().exec(command);
      BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));

      StringBuilder outputFromMavenCall = new StringBuilder();
      String line = reader.readLine();
      while (line != null) {
        if (SonarExecutor.log.isDebugEnabled()) {
          SonarExecutor.log.debug(line);
        } else if (showMavenOutputWhileAnalysis) {
          SonarExecutor.log.info(line);
        } else {
          outputFromMavenCall.append(line);
        }
        line = reader.readLine();
      }
      proc.waitFor();

      if (proc.exitValue() > 0) {
        if (!SonarExecutor.log.isDebugEnabled()) {
          SonarExecutor.log.error("call output:\n {}", outputFromMavenCall.toString());
        }
        fail("sonar test run failed");
      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  private String configureExecutionCommand() {
    StringBuilder builder = new StringBuilder("mvn -f " + pomXML.getAbsolutePath() + " sonar:sonar");

    if (jdbcDriver != null) {
      builder.append(" -Dsonar.jdbc.driver=").append(jdbcDriver);
    }

    if (jdbcUrl != null) {
      builder.append(" -Dsonar.jdbc.url=").append(jdbcUrl);
    }

    if (skipTests) {
      builder.append(" -DskipTests");
    }

    if (skipDesign) {
      builder.append(" -Dsonar.skipDesign");
    }

    if (skipDynamicAnalysis) {
      builder.append(" -Dsonar.dynamicAnalysis=false");
    }

    if (activateMavenDebug) {
      builder.append(" -X");
    }

    if (showMavenErrorWhileAnalysis) {
      builder.append(" -e");
    }
    return builder.toString();
  }

  public String getMavenVersion() throws IOException, InterruptedException {
    Process proc = Runtime.getRuntime().exec("mvn -version");
    BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));

    String line = reader.readLine();
    if (line != null) {
      return line.split("\\ +")[2];
    } else {
      return null;
    }
  }
}
