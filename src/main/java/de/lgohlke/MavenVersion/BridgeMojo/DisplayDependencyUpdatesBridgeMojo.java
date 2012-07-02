/*
 * Sonar maven checks plugin
 * Copyright (C) 2012 ${owner}
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
package de.lgohlke.MavenVersion.BridgeMojo;

import org.apache.maven.artifact.metadata.ArtifactMetadataRetrievalException;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.mojo.versions.DisplayDependencyUpdatesMojo;
import org.codehaus.mojo.versions.api.ArtifactVersions;
import org.codehaus.mojo.versions.utils.DependencyComparator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@SuppressWarnings("deprecation")
@Goal("versions:display-dependency-updates")
class DisplayDependencyUpdatesBridgeMojo extends DisplayDependencyUpdatesMojo {

  private static final String DEPENDENCIES = "Dependencies";
  private static final String DEPENDENCY_MANAGEMENT = "Dependency Management";

  private final Map<String, Map<Dependency, ArtifactVersions>> updateMap = new HashMap<String, Map<Dependency, ArtifactVersions>>();

  @SuppressWarnings("unchecked")
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    Set<Dependency> dependencyManagement = new TreeSet<Dependency>(new DependencyComparator());
    if (getProject().getDependencyManagement() != null) {
      dependencyManagement.addAll(getProject().getDependencyManagement().getDependencies());
    }

    Set<Dependency> dependencies = new TreeSet<Dependency>(new DependencyComparator());
    dependencies.addAll(getProject().getDependencies());
    if (!Boolean.FALSE.equals(processDependencyManagement)) {
      final Object[] args = new Object[] {dependencies, dependencyManagement};
      final Class<?>[] parameterTypes = new Class<?>[] {Set.class, Set.class};
      dependencies = (Set<Dependency>) MojoUtils.invokePrivateMethod(getClass().getSuperclass(), "removeDependencyManagment", args, parameterTypes);
    }

    try
    {
      if (!Boolean.FALSE.equals(processDependencyManagement)) {
        logUpdates(getHelper().lookupDependenciesUpdates(dependencyManagement, false), DEPENDENCY_MANAGEMENT);
      }
      if (!Boolean.FALSE.equals(processDependencies)) {
        logUpdates(getHelper().lookupDependenciesUpdates(dependencies, false), DEPENDENCIES);
      }
    } catch (InvalidVersionSpecificationException e)
    {
      throw new MojoExecutionException(e.getMessage(), e);
    } catch (ArtifactMetadataRetrievalException e)
    {
      throw new MojoExecutionException(e.getMessage(), e);
    }
  }

  private void logUpdates(final Map<Dependency, ArtifactVersions> updates, final String section)
  {
    updateMap.put(section, updates);
  }

  public Map<String, Map<Dependency, ArtifactVersions>> getUpdateMap() {
    return updateMap;
  }

}
