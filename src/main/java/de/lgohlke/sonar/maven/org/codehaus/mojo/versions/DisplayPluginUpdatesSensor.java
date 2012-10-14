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

import de.lgohlke.sonar.maven.BridgeMojoMapper;
import de.lgohlke.sonar.maven.MavenBaseSensor;
import de.lgohlke.sonar.maven.MavenBaseSensorI;
import de.lgohlke.sonar.maven.org.codehaus.mojo.versions.rules.IncompatibleMavenVersion;
import de.lgohlke.sonar.maven.org.codehaus.mojo.versions.rules.MissingPluginVersion;
import de.lgohlke.sonar.maven.org.codehaus.mojo.versions.rules.NoMinimumMavenVersion;
import de.lgohlke.sonar.maven.org.codehaus.mojo.versions.rules.PluginVersion;
import lombok.Getter;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.maven.MavenPluginHandler;
import org.sonar.api.resources.File;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.Violation;
import org.sonar.batch.MavenPluginExecutor;

import static de.lgohlke.sonar.maven.org.codehaus.mojo.versions.Configuration.BASE_IDENTIFIER;


public class DisplayPluginUpdatesSensor implements MavenBaseSensorI<DisplayPluginUpdatesResultHandler> {
  private final DisplayPluginUpdatesResultHandler resultHandler = new DisplayPluginUpdatesResultHandler();
  @Getter
  private final BridgeMojoMapper<DisplayPluginUpdatesResultHandler> handler = new BridgeMojoMapper<DisplayPluginUpdatesResultHandler>(DisplayPluginUpdatesBridgeMojo.class, resultHandler);
  private final MavenProject mavenProject;
  private final MavenBaseSensor<DisplayPluginUpdatesResultHandler> baseSensor;

  public DisplayPluginUpdatesSensor(MavenPluginExecutor mavenPluginExecutor,
                                    MavenProject mavenProject) {
    this.mavenProject = mavenProject;
    baseSensor = new MavenBaseSensor<DisplayPluginUpdatesResultHandler>(mavenPluginExecutor, mavenProject, BASE_IDENTIFIER, this);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  @Override
  public void analyse(final Project project, final SensorContext context) {
    DisplayPluginUpdatesResultHandler handler = this.handler.getResultTransferHandler();

    final File file = new File("", mavenProject.getFile().getName());

    // minimum version warning
    if (handler.isWarninNoMinimumVersion()) {
      Rule rule = baseSensor.createRuleFrom(NoMinimumMavenVersion.class);
      Violation violation = Violation.create(rule, file);
      violation.setLineId(1);
      violation.setMessage("Project does not define minimum Maven version, default is: 2.0");
      context.saveViolation(violation);
    }

    // incompatible minimum versions
    DisplayPluginUpdatesBridgeMojo.IncompatibleParentAndProjectMavenVersion incompatibleParentAndProjectMavenVersion = handler.getIncompatibleParentAndProjectMavenVersion();
    if (incompatibleParentAndProjectMavenVersion != null) {
      Rule rule = baseSensor.createRuleFrom(IncompatibleMavenVersion.class);
      Violation violation = Violation.create(rule, file);
      violation.setLineId(1);
      ArtifactVersion parentVersion = incompatibleParentAndProjectMavenVersion.getParentVersion();
      ArtifactVersion projectVersion = incompatibleParentAndProjectMavenVersion.getProjectVersion();
      violation.setMessage("Project does define incompatible minimum versions:  in parent pom " + parentVersion + " and in project pom " + projectVersion);
      context.saveViolation(violation);
    }

    // missing versions
    if (!handler.getMissingVersionPlugins().isEmpty()) {
      Rule rule = baseSensor.createRuleFrom(MissingPluginVersion.class);
      for (Dependency dependency : handler.getMissingVersionPlugins()) {
        Violation violation = Violation.create(rule, file);
        violation.setLineId(1);
        String artifact = dependency.getGroupId() + ":" + dependency.getArtifactId();
        violation.setMessage(artifact + " has no version");
        context.saveViolation(violation);
      }
    }

    // updates
    Rule rule = baseSensor.createRuleFrom(PluginVersion.class);
    for (ArtifactUpdate update : handler.getPluginUpdates()) {
      Violation violation = Violation.create(rule, file);
      violation.setLineId(1);
      violation.setMessage(update.toString());
      context.saveViolation(violation);
    }
  }


  @Override
  public boolean shouldExecuteOnProject(Project project) {
    return baseSensor.shouldExecuteOnProject(project);
  }

  @Override
  public MavenPluginHandler getMavenPluginHandler(Project project) {
    return baseSensor.getMavenPluginHandler(project);
  }
}
