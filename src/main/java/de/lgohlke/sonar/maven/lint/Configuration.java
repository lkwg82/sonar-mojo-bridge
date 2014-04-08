/*
 * Sonar Mojo Bridge
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.lgohlke.sonar.maven.MavenRule;
import de.lgohlke.sonar.maven.lint.rules.*;
import org.sonar.api.Extension;

import java.util.List;
import java.util.Set;

public interface Configuration {
  String BASE_IDENTIFIER = "com.lewisd:lint-maven-plugin:0.0.8:check";

  List<Class<? extends MavenRule>> RULES = ImmutableList.<Class<? extends MavenRule>>builder().
      add(LintDuplicateDependenciesRule.class).
      add(LintExecutionIdRule.class).
      add(LintGroupArtifactVersionMustBeInCorrectOrderIdRule.class).
      add(LintMissingCIManagementRule.class).
      add(LintMissingIssueManagementRule.class).
      add(LintMissingDeveloperInformationRule.class).
      add(LintProfileMustOnlyAddModulesRule.class).
      add(LintRedundantDependencyVersionsRule.class).
      add(LintRedundantPluginVersionsRule.class).
      add(LintVersionPropertiesMustUseDotVersionRule.class).
      add(LintVersionPropertiesMustUseProjectVersionRule.class).build();

  Set<Class<? extends Extension>> EXTENSIONS = ImmutableSet.<Class<? extends Extension>>builder().
          add(LintSensor.class).
          build();
}
