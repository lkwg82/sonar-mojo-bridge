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

import de.lgohlke.sonar.maven.Maven3SonarEmbedder.MavenSonarEmbedderBuilder;
import hudson.maven.MavenEmbedderException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class SonarExecutor {

  private final String jdbcDriver;
  private final String jdbcUrl;
  private boolean skipTests;
  private boolean skipDesign;
  private boolean skipDynamicAnalysis;
  private boolean activateMavenDebug;
  private boolean showMavenErrorWhileAnalysis;
  private boolean showMavenOutputWhileAnalysis;

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
  public SonarExecutor clone() {
    return new SonarExecutor(jdbcDriver, jdbcUrl).//
        activateMavenDebug(activateMavenDebug).//
        showMavenErrorWhileAnalysis(showMavenErrorWhileAnalysis).//
        showMavenOutputWhileAnalysis(showMavenOutputWhileAnalysis).//
        skipDesign(skipDesign).//
        skipDynamicAnalysis(skipDynamicAnalysis).//
        skipTests(skipTests);
  }

  public void execute() throws MavenEmbedderException {
    MavenSonarEmbedderBuilder maven3SonarEmbedderBuilder = Maven3SonarEmbedder.configure().goal("sonar:sonar");

    if (jdbcDriver != null) {
      maven3SonarEmbedderBuilder.setUserProperty("sonar.jdbc.driver", jdbcDriver);
    }

    if (jdbcUrl != null) {
      maven3SonarEmbedderBuilder.setUserProperty("sonar.jdbc.url", jdbcUrl);
    }

    if (skipTests) {
      maven3SonarEmbedderBuilder.setUserProperty("skipTests", "");
      // builder.append(" -DskipTests");
    }

    if (skipDesign) {
      maven3SonarEmbedderBuilder.setUserProperty("sonar.skipDesign", "");
      // builder.append(" -Dsonar.skipDesign");
    }

    if (skipDynamicAnalysis) {
      maven3SonarEmbedderBuilder.setUserProperty("sonar.dynamicAnalysis", "false");
      // builder.append(" -Dsonar.dynamicAnalysis=false");
    }

    if (activateMavenDebug) {
      maven3SonarEmbedderBuilder.logLevel(0);
      // builder.append(" -X");
    }

    if (showMavenErrorWhileAnalysis) {
      maven3SonarEmbedderBuilder.showErrors(showMavenErrorWhileAnalysis);
      // builder.append(" -e");
    }

    // try {
    maven3SonarEmbedderBuilder.build().run();
    // final String command = builder.toString();
    // log.info("calling : {}", command);
    // Process proc = Runtime.getRuntime().exec(command);
    // BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
    //
    // StringBuilder outputFromMavenCall = new StringBuilder();
    // String line = reader.readLine();
    // while (line != null) {
    // if (log.isDebugEnabled()) {
    // log.debug(line);
    // } else if (showMavenOutputWhileAnalysis) {
    // log.info(line);
    // } else {
    // outputFromMavenCall.append(line);
    // }
    // line = reader.readLine();
    // }
    // proc.waitFor();
    //
    // if (proc.exitValue() > 0) {
    // if (!log.isDebugEnabled()) {
    // log.error("call output:\n {}", outputFromMavenCall.toString());
    // }
    // fail("sonar test run failed");
    // }
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
  }
}
