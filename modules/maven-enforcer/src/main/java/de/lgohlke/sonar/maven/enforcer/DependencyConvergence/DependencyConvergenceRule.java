/*
 * sonar-maven-checks-maven-enforcer
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

import de.lgohlke.sonar.maven.MavenRule;
import org.sonar.check.Priority;
import org.sonar.check.Rule;

/**
 * User: lars
 */
@Rule(
    description = DependencyConvergenceRule.DESCRIPTION, //
    key = DependencyConvergenceRule.KEY, //
    name = DependencyConvergenceRule.NAME, //
    priority = Priority.MINOR
)
/**
 * @see http://maven.apache.org/enforcer/enforcer-rules/dependencyConvergence.html
 */
public interface DependencyConvergenceRule extends MavenRule {
  String DESCRIPTION = "This rule requires that dependency version numbers converge. If a project has two dependencies, A and B, both depending on the same artifact, C, this rule will fail the build if A depends on a different version of C then the version of C depended on by B.";
  String KEY = "DependencyConvergenceRule";
  String NAME = "DependencyConvergenceRule";
}
