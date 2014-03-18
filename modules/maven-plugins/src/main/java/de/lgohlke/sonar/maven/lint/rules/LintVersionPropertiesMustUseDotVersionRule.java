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
package de.lgohlke.sonar.maven.lint.rules;

import de.lgohlke.sonar.maven.MavenRule;
import org.sonar.check.Priority;
import org.sonar.check.Rule;

@Rule(
        description = LintVersionPropertiesMustUseDotVersionRule.DESCRIPTION,
        key = LintVersionPropertiesMustUseDotVersionRule.KEY,
        name = LintVersionPropertiesMustUseDotVersionRule.NAME,
        priority = Priority.MINOR

)
public interface LintVersionPropertiesMustUseDotVersionRule extends MavenRule {
    String DESCRIPTION = "The convention is to specify properties used to hold versions as \"some.library.version\", or some-library.version, " +
            "but never some-library-version or some.library-version.";
    String KEY = "lint.DotVersionProperty";
    String NAME = "[POM] version property must use dot version";
}
