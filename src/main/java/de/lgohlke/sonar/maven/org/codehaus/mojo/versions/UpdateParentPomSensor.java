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
package de.lgohlke.sonar.maven.org.codehaus.mojo.versions;

import de.lgohlke.sonar.maven.MavenBaseSensor;
import de.lgohlke.sonar.maven.ResultTransferHandler;
import de.lgohlke.sonar.maven.Rules;
import de.lgohlke.sonar.maven.SensorConfiguration;
import de.lgohlke.sonar.maven.org.codehaus.mojo.versions.rules.ParentPomVersion;
import lombok.Getter;
import lombok.Setter;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.project.MavenProject;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.File;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.Violation;
import org.sonar.batch.scan.maven.MavenPluginExecutor;
import org.sonar.plugins.xml.language.Xml;

import static de.lgohlke.sonar.maven.org.codehaus.mojo.versions.Configuration.BASE_IDENTIFIER;


@Rules(values = {ParentPomVersion.class})
@SensorConfiguration(
    bridgeMojo = UpdateParentBridgeMojo.class,
    resultTransferHandler = UpdateParentPomSensor.ResultHandler.class,
    mavenBaseIdentifier = BASE_IDENTIFIER
)
public class UpdateParentPomSensor extends MavenBaseSensor<UpdateParentPomSensor.ResultHandler> {
  @Setter
  @Getter
  public static class ResultHandler implements ResultTransferHandler {
    private String currentVersion;
    private ArtifactVersion newerVersion;
  }

  public UpdateParentPomSensor(RulesProfile rulesProfile,
                               MavenPluginExecutor mavenPluginExecutor,
                               MavenProject mavenProject) {
    super(rulesProfile, mavenPluginExecutor, mavenProject);
  }

  @Override
  public void analyse(final Project project, final SensorContext context) {
    ResultHandler resultHandler = getMojoMapper().getResultTransferHandler();
    if (resultHandler.getNewerVersion() != null) {
      Rule rule = createRuleFrom(ParentPomVersion.class);
      File file = new File("", getMavenProject().getFile().getName());
      file.setLanguage(Xml.INSTANCE);

      Violation violation = Violation.create(rule, file);
      violation.setLineId(1);

      violation.setMessage(ParentPomVersion.DESCRIPTION + ", currently used is " + resultHandler.getCurrentVersion() + " but " + resultHandler.getNewerVersion() + " is available");
      context.saveViolation(violation);
    }
  }
}
