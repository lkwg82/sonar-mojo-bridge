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
        description = LintRedundantPluginVersionsRule.DESCRIPTION,
        key = LintRedundantPluginVersionsRule.KEY,
        name = LintRedundantPluginVersionsRule.NAME,
        priority = Priority.MINOR

)
public interface LintRedundantPluginVersionsRule extends MavenRule {
    String DESCRIPTION = "Plugin versions should be set in one place, and not overridden without changing the version. " +
            "If, for example, <pluginManagement> sets a version, and <plugins> somewhere overrides it, " +
            "but with the same version, this can make version upgrades more difficult, due to the repetition.";
    String KEY = "lint.RedundantPluginVersion";
    String NAME = "[POM] redundant plugin versions";
}
