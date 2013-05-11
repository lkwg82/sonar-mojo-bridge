/*
 * sonar-maven-checks-maven-versions
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
import de.lgohlke.sonar.maven.org.codehaus.mojo.versions.rules.DependencyVersion;
import de.lgohlke.sonar.maven.org.codehaus.mojo.versions.rules.MissingPluginVersion;
import de.lgohlke.sonar.maven.org.codehaus.mojo.versions.rules.ParentPomVersion;
import org.sonar.wsclient.services.Violation;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Created with IntelliJ IDEA.
 * User: lgohlke
 * Date: 12.10.12
 * Time: 12:35
 */
public class DisplayVersionsUpdatesSensorIT extends MavenITAbstract {

  @BeforeTest(alwaysRun = true)
  public void beforeEachTest() {
    initAPI();
  }

  @Test
  public void shouldHaveHaveOldDependency() throws Exception {
    skipTestIfNotMaven3();

    final File pomXml = new File("src/test/resources/it/pom-old-dependency.xml");
    final String ruleKey = createRuleKey(DependencyVersion.KEY);

    executor.usePom(pomXml).execute();

    List<Violation> violations = getViolationsFor("org.codehaus.sonar-plugins:it-old-dependency", ruleKey);

    assertThat(violations).isNotEmpty();
    assertThat(violations).are(onlyForFile(pomXml.getName()));
  }

  @Test
  public void shouldHaveHaveOldParentPom() throws Exception {
    skipTestIfNotMaven3();

    final File pomXml = new File("src/test/resources/it/pom-old-dependency.xml");
    final String ruleKey = createRuleKey(ParentPomVersion.KEY);

    executor.usePom(pomXml).execute();

    List<Violation> violations = getViolationsFor("org.codehaus.sonar-plugins:it-old-dependency", ruleKey);

    assertThat(violations).isNotEmpty();
    assertThat(violations).are(onlyForFile(pomXml.getName()));
  }

  @Test
  public void shouldHaveViolationsOfPluginUpdate() throws Exception {
    skipTestIfNotMaven3();

    final File pomXml = new File("src/test/resources/pom_missing_maven_version.xml");
    final String ruleKey = createRuleKey(MissingPluginVersion.KEY);

    executor.usePom(pomXml).execute();

    List<Violation> violations = getViolationsFor("MavenInvoker:MavenInvoker", ruleKey);

//    api.showQueryAndResult(violations);

    assertThat(violations).isNotEmpty();
    assertThat(violations).are(onlyForFile(pomXml.getName()));
  }

  @Test(dependsOnMethods = "shouldHaveSomeViolations", enabled = false)
  public void shouldHaveImportedPom() throws Exception {
    skipTestIfNotMaven3();

    // test this http://localhost:9000/api/resources?depth=-1&scope=FIL&resource=1981&qualifier=FIL
    // not yet running
//    Resource project = api.getProjectWithKey(projectKey);
  }
}
