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

import com.google.common.collect.Lists;
import de.lgohlke.sonar.maven.MavenBaseSensor;
import de.lgohlke.sonar.maven.Rules;
import de.lgohlke.sonar.maven.SensorConfiguration;
import lombok.Getter;
import org.apache.maven.project.MavenProject;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.batch.scan.maven.MavenPluginExecutor;

import java.util.List;

import static de.lgohlke.sonar.maven.org.apache.enforcer.Configuration.BASE_IDENTIFIER;

/**
* User: lars
*/
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
public class DependencyConvergenceSensor extends MavenBaseSensor<DependencyConvergenceSensor.ResultTransferHandler> {

  public static class ResultTransferHandler implements de.lgohlke.sonar.maven.ResultTransferHandler{
    @Getter
    private List<String> errorMessages = Lists.newArrayList();
  }

  public DependencyConvergenceSensor(RulesProfile rulesProfile, MavenPluginExecutor mavenPluginExecutor, MavenProject mavenProject) {
    super(rulesProfile, mavenPluginExecutor, mavenProject);
  }

  @Override
  public void analyse(Project project, SensorContext context) {
    //To change body of implemented methods use File | Settings | File Templates.
  }
}
