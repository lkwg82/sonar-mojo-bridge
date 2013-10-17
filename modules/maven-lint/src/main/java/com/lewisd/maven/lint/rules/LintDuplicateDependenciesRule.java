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
package com.lewisd.maven.lint.rules;

import de.lgohlke.sonar.maven.MavenRule;
import org.sonar.check.Priority;
import org.sonar.check.Rule;

@Rule(
    description = LintDuplicateDependenciesRule.DESCRIPTION,
    key = LintDuplicateDependenciesRule.KEY,
    name = LintDuplicateDependenciesRule.NAME,
    priority = Priority.MAJOR

)
public interface LintDuplicateDependenciesRule extends MavenRule {
  String DESCRIPTION = "Multiple dependencies, in <dependencies> or <managedDependencies>, with the same co-ordinates are reduntant, " +
      "and can be confusing.  If they have different versions, they can lead to unexpected behaviour.";
  String KEY = "lint.DuplicateDep";
  String NAME = "[POM] found DuplicateDependencies";
}
