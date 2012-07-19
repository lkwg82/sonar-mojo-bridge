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
package de.lgohlke.sonar.maven.plugin.versions.bridgeMojos;

import de.lgohlke.sonar.maven.plugin.versions.ArtifactUpdate;

import de.lgohlke.sonar.maven.plugin.SonarAnalysisHandler;

import de.lgohlke.sonar.maven.plugin.ResultTransferHandler;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Project;

import java.util.List;
import java.util.Map;

public class DisplayDependencyUpdatesBridgeMojoResultHandler implements ResultTransferHandler<DisplayDependencyUpdatesBridgeMojoResultHandler>, SonarAnalysisHandler {

  private Map<String, List<ArtifactUpdate>> updateMap;

  public void setUpdates(final Map<String, List<ArtifactUpdate>> updateMap) {
    this.updateMap = updateMap;
  }

  @Override
  public void analyse(final Project project, final SensorContext context) {
    System.out.println(updateMap.size());
  }

}
