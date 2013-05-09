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
package de.lgohlke.sonar.maven.org.apache.enforcer;

import de.lgohlke.sonar.maven.MavenBaseSensor;
import de.lgohlke.sonar.maven.Rules;
import de.lgohlke.sonar.maven.SensorConfiguration;
import org.apache.maven.enforcer.rule.api.EnforcerRule;
import org.apache.maven.plugins.enforcer.DependencyConvergence;
import org.apache.maven.project.MavenProject;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.maven.MavenPlugin;
import org.sonar.api.batch.maven.MavenPluginHandler;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.batch.scan.maven.MavenPluginExecutor;

import java.util.List;

import static de.lgohlke.sonar.maven.org.apache.enforcer.Configuration.BASE_IDENTIFIER;

@Rules(
    values = {
        DependencyConvergenceRule.class
    }
)
@SensorConfiguration(
    bridgeMojo = EnforceBridgeMojo.class,
    resultTransferHandler = RuleTransferHandler.class,
    mavenBaseIdentifier = BASE_IDENTIFIER
)
public class EnforceSensor extends MavenBaseSensor<RuleTransferHandler> {

  public EnforceSensor(RulesProfile rulesProfile, MavenPluginExecutor mavenPluginExecutor, MavenProject mavenProject) {
    super(rulesProfile, mavenPluginExecutor, mavenProject);

    final List<EnforcerRule> rules = getMojoMapper().getResultTransferHandler().getRules();
    rules.add(new DependencyConvergence());
  }

  @Override
  public void analyse(Project project, SensorContext context) {
    System.out.println("");
  }

  @Override
  public MavenPluginHandler getMavenPluginHandler(Project project) {
    final MavenPluginHandler mavenPluginHandler = super.getMavenPluginHandler(project);

    return new MavenPluginHandler() {
      @Override
      public String getGroupId() {
        return mavenPluginHandler.getGroupId();
      }

      @Override
      public String getArtifactId() {
        return mavenPluginHandler.getArtifactId();
      }

      @Override
      public String getVersion() {
        return mavenPluginHandler.getVersion();
      }

      @Override
      public boolean isFixedVersion() {
        return mavenPluginHandler.isFixedVersion();
      }

      @Override
      public String[] getGoals() {
        return mavenPluginHandler.getGoals();
      }

      @Override
      public void configure(Project project, MavenPlugin plugin) {
        plugin.setParameter("rules/DependencyConvergence",null);
      }
    };
  }
}
