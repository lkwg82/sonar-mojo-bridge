/*
 * Sonar mojo bridge plugin
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
package de.lgohlke.sonar.maven.enforcer.DependencyConvergence;

import de.lgohlke.sonar.maven.enforcer.EnforceMavenPluginHandler;
import de.lgohlke.sonar.maven.enforcer.Violation;
import lombok.Setter;
import org.apache.maven.plugins.enforcer.report.Dependency;
import org.apache.maven.plugins.enforcer.report.DependencyConvergenceReport;
import org.apache.maven.plugins.enforcer.report.DependencyConvergenceViolation;
import org.fest.assertions.data.MapEntry;
import org.testng.annotations.Test;

import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

public class DependencyConvergenceViolationAdapterTest {

    @Test
    public void testConfiguredMavenPluginHandler() {
        DependencyConvergenceViolationAdapter adapter = new DependencyConvergenceViolationAdapter();
        EnforceMavenPluginHandler handler = new EnforceMavenPluginHandler(null);
        adapter.configure(handler);

        assertThat(handler.getParameters()).contains(MapEntry.entry("rules/DependencyConvergence", null));
        assertThat(handler.getParameters()).containsKey("rules/DependencyConvergence/xmlReport");
    }

    class MyDependencyConvergenceViolationAdapter extends DependencyConvergenceViolationAdapter {
        @Setter
        private DependencyConvergenceReport report;

        @Override
        protected DependencyConvergenceReport getReport() {
            return report;
        }
    }

    @Test
    public void testGetViolations() {

        Dependency dependency = new Dependency();
        dependency.setGroupId("g");
        dependency.setArtifactId("a");

        DependencyConvergenceViolation violation = new DependencyConvergenceViolation();
        violation.setDependency(dependency);
        violation.getVersions().add("1");
        violation.getVersions().add("2");
        violation.getVersions().add("3");

        DependencyConvergenceReport report = new DependencyConvergenceReport();
        report.getDependencyConvergencesViolations().add(violation);

        MyDependencyConvergenceViolationAdapter adapter = new MyDependencyConvergenceViolationAdapter();
        adapter.setReport(report);

        List<Violation> violations = adapter.getViolations();

        assertThat(violations).hasSize(1);

        Violation firstViolation = violations.get(0);

        assertThat(firstViolation.getLine()).isEqualTo(1);

        String expected = "found multiple version for g:a (3,2,1)";
        assertThat(firstViolation.getMessage()).isEqualTo(expected);
    }
}
