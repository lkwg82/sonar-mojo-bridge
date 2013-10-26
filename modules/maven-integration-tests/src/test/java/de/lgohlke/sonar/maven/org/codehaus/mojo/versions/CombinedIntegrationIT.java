/*
 * sonar-mojo-bridge-integration
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
package de.lgohlke.sonar.maven.org.codehaus.mojo.versions;

import de.lgohlke.sonar.maven.MavenITAbstract;
import org.sonar.wsclient.issue.Issue;
import org.sonar.wsclient.issue.Issues;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;

import static org.fest.assertions.api.Assertions.assertThat;

public class CombinedIntegrationIT extends MavenITAbstract {
  @BeforeTest(alwaysRun = true)
  public void beforeEachTest() {
    initAPI();
  }

  @Test
  public void shouldHaveHaveOldDependency() throws Exception {
    skipTestIfNotMaven3();

    File pomXml = new File("src/test/resources/pom-old-dependency.xml");
    String ruleKey = createRuleKey("Old Dependency");

    executor.usePom(pomXml).execute();

    String projectKey = "org.codehaus.sonar-plugins:it-old-dependency";
    Issues issues = getIssuesFor(projectKey, ruleKey);

    assertThat(issues.list()).hasSize(1);
    assertThatIssuesOnlyFromThisPom(pomXml, projectKey, issues);
  }

  @Test
  public void shouldHaveHaveOldParentPom() throws Exception {
    skipTestIfNotMaven3();

    File pomXml = new File("src/test/resources/pom-old-dependency.xml");
    String ruleKey = createRuleKey("Old Parent Pom");

    executor.usePom(pomXml).execute();

    String projectKey = "org.codehaus.sonar-plugins:it-old-dependency";
    Issues issues = getIssuesFor(projectKey, ruleKey);

    assertThat(issues.list()).isNotEmpty();
    assertThatIssuesOnlyFromThisPom(pomXml, projectKey, issues);
  }

  @Test
  public void shouldHaveViolationsOfPluginUpdate() throws Exception {
    skipTestIfNotMaven3();

    File pomXml = new File("src/test/resources/pom_missing_maven_version.xml");
    String ruleKey = createRuleKey("Missing Plugin Version");

    executor.usePom(pomXml).execute();

    String projectKey = "MavenInvoker:MavenInvoker";
    Issues issues = getIssuesFor(projectKey, ruleKey);

    assertThat(issues.list()).isNotEmpty();

    assertThatIssuesOnlyFromThisPom(pomXml, projectKey, issues);
  }

  @Test
  public void shouldHaveIssueWithDependencyConvergence() throws Exception {
    skipTestIfNotMaven3();

    File pomXml = new File("src/test/resources/pom_dependencyConvergence.xml");
    String ruleKey = createRuleKey("DependencyConvergence");

    executor.usePom(pomXml).execute();

    String projectKey = "MavenInvoker:MavenInvoker";
    Issues issues = getIssuesFor(projectKey, ruleKey);

    assertThat(issues.list()).isNotEmpty();
    assertThatIssuesOnlyFromThisPom(pomXml, projectKey, issues);
  }

  private void assertThatIssuesOnlyFromThisPom(File pomXml, String projectKey, Issues issues) {
    for (Issue issue : issues.list()) {
      assertThat(issue.componentKey()).isEqualTo(projectKey + ":" + pomXml.getName());
    }
  }
}
