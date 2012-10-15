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

import com.google.common.base.Preconditions;
import de.lgohlke.sonar.MavenPlugin;
import de.lgohlke.sonar.SonarAPIWrapper;
import de.lgohlke.sonar.SonarExecutor;
import org.fest.assertions.core.Condition;
import org.sonar.wsclient.services.Resource;
import org.sonar.wsclient.services.Violation;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;

import java.io.IOException;
import java.util.List;


public abstract class MavenITAbstract {
  private static final String SONAR_HOST = "http://localhost:9000";
  protected SonarExecutor executor;
  protected SonarAPIWrapper api;

  @BeforeClass
  public void beforeAllTests() {
    String jdbcDriver = System.getProperty("jdbcDriver");
    String jdbcUrl = System.getProperty("jdbcUrl");

    executor =
        new SonarExecutor(jdbcDriver, jdbcUrl) //
            .skipDesign() //
            .skipDynamicAnalysis() //
            .skipTests() //
            .showMavenErrorWhileAnalysis() //
            .showMavenOutputWhileAnalysis();

    System.getProperties()
        .put(Maven3SonarEmbedder.MavenSonarEmbedderBuilder.M2_HOME, Maven3SonarEmbedderTest.MAVEN_HOME);
  }

  public void initAPI() {
    api = new SonarAPIWrapper(SONAR_HOST);
  }

  protected void skipTestIfNotMaven3() throws IOException, InterruptedException {
    final String mavenVersion = executor.getMavenVersion();
    if (mavenVersion.startsWith("2")) {
      throw new SkipException("could not proceed, because these tests only support maven3");
    }
  }

  protected List<Violation> getViolationsFor(final String projectKey, final String ruleKey) {
    Preconditions.checkNotNull(api, "please call initAPI() before each test");

    Resource projectResource = api.getProjectWithKey(projectKey);
    return api.getViolationsFor(projectResource.getId(), ruleKey);
  }

  protected String createRuleKey(final String specificRuleKey) {
    return MavenPlugin.REPOSITORY_KEY + ":" + specificRuleKey;
  }

  protected Condition<Violation> onlyForFile(final String filename) {
    return new Condition<Violation>() {
      @Override
      public boolean matches(final Violation violation) {
        final boolean isScope = violation.getResourceScope().equals(SonarAPIWrapper.SCOPES.FIL.name());
        final boolean isQualifier = violation.getResourceQualifier().equals(SonarAPIWrapper.QUALIFIERS.FIL.name());
        return isScope && isQualifier && violation.getResourceName().equals(filename);
      }
    };
  }
}
