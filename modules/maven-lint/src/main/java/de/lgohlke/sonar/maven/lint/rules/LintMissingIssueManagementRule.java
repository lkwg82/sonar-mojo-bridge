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
package de.lgohlke.sonar.maven.lint.rules;

import de.lgohlke.sonar.maven.MavenRule;
import org.sonar.check.Priority;
import org.sonar.check.Rule;

@Rule(
    description = LintMissingIssueManagementRule.DESCRIPTION,
    key = LintMissingIssueManagementRule.KEY,
    name = LintMissingIssueManagementRule.NAME,
    priority = Priority.MAJOR

)
public interface LintMissingIssueManagementRule extends MavenRule {
  String DESCRIPTION = "For reporting bugs or request features give a linkto your issue tracker.";
  String KEY = "lint.OSSIssueManagementSectionRule";
  String NAME = "[POM] missing section of issue-management";
}
