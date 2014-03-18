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
package de.lgohlke.sonar.maven.lint.rules;

import de.lgohlke.sonar.maven.MavenRule;
import org.sonar.check.Priority;
import org.sonar.check.Rule;

@Rule(
        description = LintGroupArtifactVersionMustBeInCorrectOrderIdRule.DESCRIPTION,
        key = LintGroupArtifactVersionMustBeInCorrectOrderIdRule.KEY,
        name = LintGroupArtifactVersionMustBeInCorrectOrderIdRule.NAME,
        priority = Priority.MINOR

)
public interface LintGroupArtifactVersionMustBeInCorrectOrderIdRule extends MavenRule {
    String DESCRIPTION = "Maven convention is that the groupId, artifactId, and version elements be listed in that order.  " +
            "Other elements with short, simple content, such as type, scope, classifier, etc, should be before elements " +
            "with longer content, such as configuration, executions, and exclusions, otherwise they can be easily missed, " +
            "leading to confusion";
    String KEY = "lint.GAVOrder";
    String NAME = "[POM] ordering of artifact";
}
