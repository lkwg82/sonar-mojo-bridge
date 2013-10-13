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
    description = LintMissingDeveloperInformationRule.DESCRIPTION,
    key = LintMissingDeveloperInformationRule.KEY,
    name = LintMissingDeveloperInformationRule.NAME,
    priority = Priority.MAJOR

)
public interface LintMissingDeveloperInformationRule extends MavenRule {
  String DESCRIPTION = "The users/developers need to know where to get active bugs and to report new ones to.";
  String KEY = "lint.OSSDevelopersSectionRule";
  String NAME = "[POM] missing section of developers";
}
