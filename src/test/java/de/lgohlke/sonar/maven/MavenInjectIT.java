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

import de.lgohlke.sonar.MavenPlugin;
import de.lgohlke.sonar.maven.org.codehaus.mojo.versions.rules.DependencyVersionMavenRule;
import org.fest.assertions.core.Condition;
import org.sonar.wsclient.services.Resource;
import org.sonar.wsclient.services.Violation;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

public class MavenInjectIT {
  private final static String SONAR_HOST = "http://localhost:9000";
  private SonarExecutor executor;
  private SonarAPIWrapper api;

  @BeforeClass
  public void beforeAllTests() {
    String jdbcDriver = System.getProperty("jdbcDriver");
    String jdbcUrl = System.getProperty("jdbcUrl");

    executor = new SonarExecutor(jdbcDriver, jdbcUrl).//
        skipDesign().//
        skipDynamicAnalysis().//
        skipTests().//
        showMavenErrorWhileAnalysis().//
        showMavenOutputWhileAnalysis().activateMavenDebug();
    System.getProperties().put(Maven3SonarEmbedder.MavenSonarEmbedderBuilder.M2_HOME, Maven3SonarEmbedderTest.MAVEN_HOME);
  }

  @BeforeTest
  public void beforeEachTest() {
    api = new SonarAPIWrapper(SONAR_HOST);
  }

  @Test(enabled = false)
  public void shouldExecuteInstalledPluginWithoutErrors() throws Exception {

    executor.execute();
  }

  @Test(enabled = false)
  public void shouldHaveSomeViolations() throws Exception {
    final String projectKey = "org.codehaus.sonar-plugins:sonar-maven-checks";
    final String ruleKey = createRuleKey(DependencyVersionMavenRule.KEY);

    executor.execute();
    List<Violation> violations = getViolationsFor(projectKey, ruleKey);

    api.showQueryAndResult(violations);

    assertThat(violations).isNotEmpty();
    assertThat(violations).are(onlyForFile("pom.xml"));
  }

  private List<Violation> getViolationsFor(final String projectKey, final String ruleKey) {
    Resource projectResource = api.getProjectWithKey(projectKey);
    return api.getViolationsFor(projectResource.getId(), ruleKey);
  }

  private String createRuleKey(final String specificRuleKey) {
    return MavenPlugin.REPOSITORY_KEY + ":" + specificRuleKey;
  }

  private Condition<Violation> onlyForFile(final String filename) {
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
