/*
 * sonar-mojo-bridge-maven-plugins
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

import de.lgohlke.sonar.maven.MavenRule;
import de.lgohlke.sonar.maven.Rules;
import de.lgohlke.sonar.maven.XmlReader;
import de.lgohlke.sonar.maven.lint.xml.Results;
import de.lgohlke.sonar.maven.lint.xml.Violation;
import org.apache.commons.io.FileUtils;
import org.apache.maven.project.MavenProject;
import org.reflections.Reflections;
import org.sonar.api.batch.maven.MavenPluginHandler;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.Rule;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class LintSensorTest {

    private MavenProject mavenProject;
    private LintSensor sensor;

    @BeforeTest(alwaysRun = true)
    public void beforeTest() {
        mavenProject = new MavenProject();
        mavenProject.setFile(new File("."));

        RulesProfile rulesProfile = RulesProfile.create("mine", "java");
        sensor = new LintSensor(mavenProject, rulesProfile, mock(ResourcePerspectives.class), mock(Settings.class));
    }

    @Test
    public void testNotImplementedNullReturn() {

        Violation violation = new Violation();
        violation.setRule("xy");

        Rule rule = sensor.createRuleFromViolation(violation);

        assertThat(rule).isNull();
    }

    @Test
    public void testRuleMatchingViolation() {

        Violation violation = new Violation();
        violation.setRule("DuplicateDep");

        Rule ruleFromViolation = sensor.createRuleFromViolation(violation);

        assertThat(ruleFromViolation).isNotNull();
    }

    @Test
    public void testConfiguredAllRulesInAnnotation() {

        class StringComparator implements Comparator<String> {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        }

        Reflections reflections = new Reflections("de.lgohlke.sonar.maven.lint.rules");
        Set<Class<? extends MavenRule>> rulesImplemented = reflections.getSubTypesOf(MavenRule.class);

        Rules rules = LintSensor.class.getAnnotation(Rules.class);

        TreeSet<String> configuredRules = new TreeSet<String>(new StringComparator());
        for (Class clazz : rules.values()) {
            configuredRules.add(clazz.getCanonicalName());
        }
        TreeSet<String> implementedRules = new TreeSet<String>(new StringComparator());
        for (Class clazz : rulesImplemented) {
            implementedRules.add(clazz.getCanonicalName());
        }

        assertThat(configuredRules).isEqualTo(implementedRules);
    }

    @Test
    public void testMavenHandler() {
        MavenPluginHandler mavenPluginHandler = sensor.getMavenPluginHandler(mock(Project.class));

        assertThat(mavenPluginHandler.getGoals()).hasSize(1);
        assertThat(mavenPluginHandler.getGoals()).contains("check");
        assertThat(mavenProject.getProperties()).containsKey("maven-lint.failOnViolation");
        assertThat(mavenProject.getProperties()).containsKey("maven-lint.output.file.xml");
    }

    @Test
    public void testXmlDeserialisationWithZeroResults() throws IOException {
        String xml = "<results status=\"PASS\" violations=\"0\"/>";

        Results results = getResultsFromXml(xml);

        assertThat(results.getViolations()).isNull();
    }

    @Test
    public void testXmlDeserialisationWithSomeResults() throws IOException {
        String xml = "" +
                "<results status=\"FAIL\" violations=\"1\">\n" +
                "  <violation rule=\"DuplicateDep\">\n" +
                "    <message>Dependency &apos;org.codehaus.sonar:sonar-maven3-plugin:jar&apos; is declared multiple times with the same version: 47:17</message>\n" +
                "    <description>Multiple dependencies, in &lt;dependencies&gt; or &lt;managedDependencies&gt;, with the same co-ordinates are reduntant, and can be confusing.  If they have different versions, they can lead to unexpected behaviour.</description>\n" +
                "    <location file=\"/home/lars/development/workspaces/sonar/sonar-mojo-bridge/modules/maven-lint/pom.xml\" line=\"41\" column=\"17\"/>\n" +
                "  </violation>\n" +
                "</results>";

        Results results = getResultsFromXml(xml);

        assertThat(results.getViolations()).hasSize(1);
        assertThat(results.getViolations().get(0).getLocation().getLine()).isEqualTo(41);
    }

    private Results getResultsFromXml(String xml) throws IOException {
        File tempFile = File.createTempFile(Math.random() + "", Math.random() + "");
        FileUtils.write(tempFile, xml);
        tempFile.deleteOnExit();

        File projectDir = tempFile.getParentFile();
        String xmlReport = tempFile.getAbsoluteFile().getName();
        return new XmlReader().readXmlFromFile(projectDir, xmlReport, Results.class);
    }
}
