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
package de.lgohlke.sonar.maven.org.codehaus.mojo.versions;

import de.lgohlke.sonar.maven.MavenITAbstract;
import de.lgohlke.sonar.maven.org.codehaus.mojo.versions.rules.DependencyVersionMavenRule;
import org.sonar.wsclient.services.Violation;
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
  @Test
  public void shouldHaveSomeViolations() throws Exception {
    skipTestIfNotMaven3();

    final File pomXml = new File("src/test/resources/it/pom-old-dependency.xml");
    final String projectKey = "org.codehaus.sonar-plugins:it-old-dependency";
    final String ruleKey = createRuleKey(DependencyVersionMavenRule.KEY);


    executor.usePom(pomXml).execute();

    List<Violation> violations = getViolationsFor(projectKey, ruleKey);

    // api.showQueryAndResult(violations);

    assertThat(violations).isNotEmpty();
    assertThat(violations).are(onlyForFile(pomXml.getName()));
  }
}
