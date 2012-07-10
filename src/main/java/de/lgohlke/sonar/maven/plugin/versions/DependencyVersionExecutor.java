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
package de.lgohlke.sonar.maven.plugin.versions;

import de.lgohlke.sonar.maven.MojoExecutionHandler;
import de.lgohlke.sonar.maven.plugin.DefaultMavenGoalExecutorImpl;
import de.lgohlke.sonar.plugin.MavenRule;
import org.apache.maven.model.Dependency;
import org.codehaus.mojo.versions.DisplayDependencyUpdatesMojo;
import org.codehaus.mojo.versions.api.ArtifactVersions;
import org.codehaus.mojo.versions.api.UpdateScope;
import org.sonar.api.rules.Violation;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class DependencyVersionExecutor extends DefaultMavenGoalExecutorImpl {

  private final MojoExecutionHandler<DisplayDependencyUpdatesMojo, DisplayDependencyUpdatesBridgeMojo> mojoExectionHandler = new MojoExecutionHandler<DisplayDependencyUpdatesMojo, DisplayDependencyUpdatesBridgeMojo>() {

    @Override
    protected void beforeExecution2(final DisplayDependencyUpdatesBridgeMojo mojo) {
      // ok
    }

    @Override
    protected void afterExecution2(final DisplayDependencyUpdatesBridgeMojo mojo) {

      for (Entry<String, Map<Dependency, ArtifactVersions>> entry : mojo.getUpdateMap().entrySet()) {
        final String section = entry.getKey();
        for (Entry<Dependency, ArtifactVersions> updateEntry : entry.getValue().entrySet()) {
          final Dependency dependency = updateEntry.getKey();
          final ArtifactVersions artifactVersions = updateEntry.getValue();
          createUpdateViolation(section, dependency, artifactVersions);
        }
      }
    }

    private void createUpdateViolation(final String section, final Dependency dependency, final ArtifactVersions artifactVersions) {

      Violation violation = createViolation(new DependencyVersionMavenRule());
      String message = String.format("[%s] %s new version available %s", section, dependency, artifactVersions.getNewestUpdate(UpdateScope.ANY));
      violation.setMessage(message);
      getContext().saveViolation(violation);
    }

    @Override
    public Class<DisplayDependencyUpdatesMojo> getOriginalMojo() {
      return DisplayDependencyUpdatesMojo.class;
    }

    @Override
    public Class<DisplayDependencyUpdatesBridgeMojo> getReplacingMojo() {
      return DisplayDependencyUpdatesBridgeMojo.class;
    }
  };

  @Override
  protected List<? extends MavenRule> getMavenRules() {
    return Arrays.asList(new DependencyVersionMavenRule());
  }

  @Override
  protected MojoExecutionHandler<?, ?> getMojoExectionHandler() {
    return mojoExectionHandler;
  }

}
