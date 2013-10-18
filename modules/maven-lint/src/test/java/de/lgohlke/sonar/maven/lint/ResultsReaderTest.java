/*
 * sonar-mojo-bridge-maven-lint
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
package de.lgohlke.sonar.maven.lint;

import org.testng.annotations.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class ResultsReaderTest {

    @Test
    public void test() {
        String xml = "<results status=\"PASS\" violations=\"0\"/>";
        de.lgohlke.sonar.maven.lint.xml.Results results = new ResultsReader().read(xml);

        assertThat(results.getViolations()).isEmpty();
    }

    @Test
    public void test2() {
        String xml = "" +
                "<results status=\"FAIL\" violations=\"1\">\n" +
                "  <violation rule=\"DuplicateDep\">\n" +
                "    <message>Dependency &apos;org.codehaus.sonar:sonar-maven3-plugin:jar&apos; is declared multiple times with the same version: 47:17</message>\n" +
                "    <description>Multiple dependencies, in &lt;dependencies&gt; or &lt;managedDependencies&gt;, with the same co-ordinates are reduntant, and can be confusing.  If they have different versions, they can lead to unexpected behaviour.</description>\n" +
                "    <location file=\"/home/lars/development/workspaces/sonar/sonar-mojo-bridge/modules/maven-lint/pom.xml\" line=\"41\" column=\"17\"/>\n" +
                "  </violation>\n" +
                "</results>";

        de.lgohlke.sonar.maven.lint.xml.Results results = new ResultsReader().read(xml);

        assertThat(results.getViolations()).hasSize(1);
        assertThat(results.getViolations().get(0).getLocation().getLine()).isEqualTo(41);
    }
}
