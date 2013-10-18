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

import de.lgohlke.sonar.maven.MavenRule;
import de.lgohlke.sonar.maven.lint.rules.LintDuplicateDependenciesRule;
import de.lgohlke.sonar.maven.lint.rules.LintExecutionIdRule;
import de.lgohlke.sonar.maven.lint.rules.LintGroupArtifactVersionMustBeInCorrectOrderIdRule;
import de.lgohlke.sonar.maven.lint.rules.LintMissingCIManagementRule;
import de.lgohlke.sonar.maven.lint.rules.LintMissingDeveloperInformationRule;
import de.lgohlke.sonar.maven.lint.rules.LintProfileMustOnlyAddModulesRule;
import de.lgohlke.sonar.maven.lint.rules.LintRedundantDependencyVersionsRule;
import de.lgohlke.sonar.maven.lint.rules.LintRedundantPluginVersionsRule;
import de.lgohlke.sonar.maven.lint.rules.LintVersionPropertiesMustUseDotVersionRule;
import de.lgohlke.sonar.maven.lint.rules.LintVersionPropertiesMustUseProjectVersionRule;

import java.util.ArrayList;
import java.util.List;

public interface Configuration {
    String BASE_IDENTIFIER = "com.lewisd:lint-maven-plugin:0.0.8:check";

    List<Class<? extends MavenRule>> RULE_IMPLEMENTATION_REPOSITORY = new ArrayList<Class<? extends MavenRule>>() {
        {
            add(LintDuplicateDependenciesRule.class);
            add(LintExecutionIdRule.class);
            add(LintGroupArtifactVersionMustBeInCorrectOrderIdRule.class);
            add(LintMissingCIManagementRule.class);
            add(LintMissingDeveloperInformationRule.class);
            add(LintProfileMustOnlyAddModulesRule.class);
            add(LintRedundantDependencyVersionsRule.class);
            add(LintRedundantPluginVersionsRule.class);
            add(LintVersionPropertiesMustUseDotVersionRule.class);
            add(LintVersionPropertiesMustUseProjectVersionRule.class);
        }
    };
}
