/*
 * sonar-mojo-bridge-maven-enforcer
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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import de.lgohlke.sonar.maven.XmlReader;
import de.lgohlke.sonar.maven.enforcer.ConfigurableEnforceMavenPluginHandler;
import de.lgohlke.sonar.maven.enforcer.Violation;
import de.lgohlke.sonar.maven.enforcer.ViolationAdapter;
import org.apache.maven.plugins.enforcer.report.Dependency;
import org.apache.maven.plugins.enforcer.report.DependencyConvergenceReport;
import org.apache.maven.plugins.enforcer.report.DependencyConvergenceViolation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * User: lgohlke
 */
public class DependencyConvergenceViolationAdapter extends ViolationAdapter {

    private final static String XML_REPORT = "target/enforcer_dependencyConvergence_report.xml";

    @Override
    public void configure(ConfigurableEnforceMavenPluginHandler handler) {
        handler.setParameter("fail", "false");
        handler.setParameter("rules/DependencyConvergence", null);
        handler.setParameter("rules/DependencyConvergence/xmlReport", XML_REPORT);
    }

    @Override
    public List<Violation> getViolations() {

        List<Violation> violations = new ArrayList<Violation>();

        DependencyConvergenceReport report = getReport();

        for (DependencyConvergenceViolation dcViolation : report.getDependencyConvergencesViolations()) {
            Violation violation = new Violation();
            violation.setLine(1);
            violation.setMessage(createMessage(dcViolation));

            violations.add(violation);
        }
        return violations;
    }

    @VisibleForTesting
    protected DependencyConvergenceReport getReport() {
        return new XmlReader().readXmlFromFile(getProjectDir(), XML_REPORT, DependencyConvergenceReport.class);
    }

    private String createMessage(DependencyConvergenceViolation violation) {
        Dependency artifact = violation.getDependency();
        Set<String> versions = violation.getVersions();

        return "found multiple version for " + artifact.getGroupId() + ":" + artifact.getArtifactId() + " (" + Joiner.on(",").join(versions) + ")";
    }
}
