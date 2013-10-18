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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.lewisd.maven.lint.Results;
import com.lewisd.maven.lint.Violation;
import de.lgohlke.sonar.maven.lint.xml.Results;
import de.lgohlke.sonar.maven.lint.xml.Violation;
import de.lgohlke.sonar.maven.MavenRule;
import de.lgohlke.sonar.maven.Rules;
import de.lgohlke.sonar.maven.lint.LintSensor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.NotImplementedException;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;
import org.reflections.Reflections;
import org.sonar.api.batch.maven.MavenPluginHandler;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.Rule;
import org.sonar.batch.DefaultSensorContext;

import java.io.File;
import java.util.List;
import java.util.Set;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class LintSensorTest {

    private MavenProject mavenProject = getMavenProject();
    private RulesProfile rulesProfile;

    @Before
    public void beforeTest() {
        rulesProfile = RulesProfile.create("mine", "java");
    }

    @Test(expected = NotImplementedException.class)
    public void testNotImplementedViolation() {

        Violation violation = new Violation();
        violation.setRule("xy");

        LintSensor sensor = new LintSensor(mavenProject, rulesProfile);
        sensor.createRuleFromViolation(violation);
    }

    @Test
    public void testRuleMatchingViolation() {

        Violation violation = new Violation();
        violation.setRule("DuplicateDep");

        LintSensor sensor = new LintSensor(mavenProject, rulesProfile);
        Rule ruleFromViolation = sensor.createRuleFromViolation(violation);

        assertThat(ruleFromViolation).isNotNull();
    }

    @Test
    public void testConfiguredAllRulesInAnnotation() {
        Reflections reflections = new Reflections("com.lewisd.maven.lint.rules");
        Set<Class<? extends MavenRule>> rulesImplemented = reflections.getSubTypesOf(MavenRule.class);

        Rules rules = LintSensor.class.getAnnotation(Rules.class);

        assertThat(Sets.newHashSet(rules.values())).isEqualTo(rulesImplemented);
    }

    @Test
    public void testMavenHandler() {
        LintSensor sensor = new LintSensor(mavenProject, rulesProfile);
        MavenPluginHandler mavenPluginHandler = sensor.getMavenPluginHandler(mock(Project.class));

        assertThat(mavenPluginHandler.getGoals()).hasSize(1);
        assertThat(mavenPluginHandler.getGoals()).contains("check");
        assertThat(mavenProject.getProperties()).containsKey("maven-lint.failOnViolation");
        assertThat(mavenProject.getProperties()).containsKey("maven-lint.output.file.xml");
    }

    static MavenProject getMavenProject() {
        MavenProject mavenProject = new MavenProject();
        mavenProject.setFile(new File("."));
        return mavenProject;
    }

    public static class MyLintSensor extends LintSensor {
        @Setter
        private Results results;

        @Setter
        private String xml;

        public MyLintSensor(MavenProject mavenProject, RulesProfile rulesProfile) {
            super(mavenProject, rulesProfile);
        }

        @Override
        Results getResults(String xml) {
            return results == null ? super.getResults(xml) : results;
        }

        @Override
        String getXmlFromReport() {
            return xml == null ? "" : xml;
        }

    }

    private static class TestSensorContext extends DefaultSensorContext {
        @Getter
        List<org.sonar.api.rules.Violation> violations = Lists.newArrayList();

        public TestSensorContext() {
            super(null, null);
        }

        @Override
        public void saveViolation(org.sonar.api.rules.Violation violation) {
            violations.add(violation);
        }
    }
}
