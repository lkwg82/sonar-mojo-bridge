/*
 * Sonar maven checks plugin
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
package de.lgohlke.sonar;

import de.lgohlke.sonar.maven.org.apache.maven.plugins.enforcer.DependencyConvergence.DependencyConvergenceRule;
import de.lgohlke.sonar.maven.org.codehaus.mojo.versions.rules.*;
import org.sonar.api.rules.AnnotationRuleParser;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleRepository;

import java.util.Arrays;
import java.util.List;

public class RulesRepository extends RuleRepository {
  private final AnnotationRuleParser ruleParser;

  public RulesRepository(final AnnotationRuleParser ruleParser) {
    super(MavenPlugin.REPOSITORY_KEY, "java");
    setName(MavenPlugin.REPOSITORY_NAME);
    this.ruleParser = ruleParser;
  }

  @Override
  public List<Rule> createRules() {
    return ruleParser.parse(MavenPlugin.REPOSITORY_KEY, getCheckedClasses());
  }

  @SuppressWarnings("rawtypes")
  private static List<Class> getCheckedClasses() {
    return Arrays.asList((Class) DependencyVersion.class, PluginVersion.class, MissingPluginVersion.class,
        IncompatibleMavenVersion.class, NoMinimumMavenVersion.class, ParentPomVersion.class, DependencyConvergenceRule.class);
  }

}
