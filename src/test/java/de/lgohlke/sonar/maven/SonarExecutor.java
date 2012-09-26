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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import static org.fest.assertions.api.Assertions.fail;

@Slf4j
@RequiredArgsConstructor
final class SonarExecutor {

  private final static String BASE_CMD_TEMPLATE = "mvn sonar:sonar -Dsonar.jdbc.url=%s -Dsonar.jdbc.driver=%s";
  private final String jdbcDriver;
  private final String jdbcUrl;
  private boolean skipTests;
  private boolean skipDesign;
  private boolean skipDynamicAnalysis;
  private boolean activateMavenDebug;
  private boolean showMavenErrorWhileAnalysis;

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

  @Override
  public SonarExecutor clone() {
    return new SonarExecutor(jdbcDriver, jdbcUrl).//
        activateMavenDebug(activateMavenDebug).//
        showMavenErrorWhileAnalysis(showMavenErrorWhileAnalysis).//
        skipDesign(skipDesign).//
        skipDynamicAnalysis(skipDynamicAnalysis).//
        skipTests(skipTests);
  }

  public void execute() {
    StringBuilder builder = new StringBuilder(String.format(BASE_CMD_TEMPLATE, jdbcUrl, jdbcDriver));

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

    try {
      final String command = builder.toString();
      log.info("calling : {}", command);
      Process proc = Runtime.getRuntime().exec(command);
      BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));

      StringBuilder outputFromMavenCall = new StringBuilder();
      String line = reader.readLine();
      while (line != null) {
        if (log.isDebugEnabled()) {
          log.debug(line + "\n");
        } else {
          outputFromMavenCall.append(line);
          outputFromMavenCall.append("\n");
        }
        line = reader.readLine();
      }
      proc.waitFor();

      if (proc.exitValue() > 0) {
        if (!log.isDebugEnabled()) {
          log.error("call output:\n {}", outputFromMavenCall.toString());
        }
        fail("sonar test run failed");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
