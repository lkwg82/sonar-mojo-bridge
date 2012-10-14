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

import com.google.common.collect.Maps;
import de.lgohlke.sonar.maven.BridgeMojo;
import de.lgohlke.sonar.maven.Goal;
import lombok.Setter;
import org.apache.maven.artifact.metadata.ArtifactMetadataRetrievalException;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.mojo.versions.DisplayDependencyUpdatesMojo;
import org.codehaus.mojo.versions.api.ArtifactVersions;
import org.codehaus.mojo.versions.api.UpdateScope;
import org.codehaus.mojo.versions.utils.DependencyComparator;
import org.fest.reflect.reference.TypeRef;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;
import static org.fest.reflect.core.Reflection.staticMethod;


@Goal(Configuration.Goals.DISPLAY_DEPENDENCY_UPDATES)
@SuppressWarnings("deprecation")
public class DisplayDependencyUpdatesBridgeMojo extends DisplayDependencyUpdatesMojo implements BridgeMojo<DisplayUpdatesBridgeMojoResultHandler> {
  public static final String DEPENDENCY_MANAGEMENT = "Dependency Management";
  public static final String DEPENDENCIES = "Dependencies";
  private final Map<String, List<ArtifactUpdate>> updateMap = Maps.newHashMap();

  private Boolean processDependencyManagement;
  private Boolean processDependencies;
  @Setter
  private DisplayUpdatesBridgeMojoResultHandler resultHandler;

  public DisplayDependencyUpdatesBridgeMojo() {
    super();
  }

  @Override
  @SuppressWarnings("unchecked")
  public void execute() throws MojoExecutionException, MojoFailureException {
    Set<Dependency> dependencyManagement = new TreeSet<Dependency>(new DependencyComparator());
    if (getProject().getDependencyManagement() != null) {
      dependencyManagement.addAll(getProject().getDependencyManagement().getDependencies());
    }

    Set<Dependency> dependencies = new TreeSet<Dependency>(new DependencyComparator());
    dependencies.addAll(getProject().getDependencies());
    if (!Boolean.FALSE.equals(processDependencyManagement)) {
      dependencies = removeDependencyManagment(dependencies, dependencyManagement);
    }

    try {
      if (!Boolean.FALSE.equals(processDependencyManagement)) {
        logUpdates(getHelper().lookupDependenciesUpdates(dependencyManagement, false), DEPENDENCY_MANAGEMENT);
      }
      if (!Boolean.FALSE.equals(processDependencies)) {
        logUpdates(getHelper().lookupDependenciesUpdates(dependencies, false), DEPENDENCIES);
      }
    } catch (InvalidVersionSpecificationException e) {
      throw new MojoExecutionException(e.getMessage(), e);
    } catch (ArtifactMetadataRetrievalException e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }

    resultHandler.setUpdateMap(updateMap);
  }

  /**
   * calling private static methods from super class {@link DisplayDependencyUpdatesMojo#removeDependencyManagment( Set, Set )}
   *
   */
  private Set<Dependency> removeDependencyManagment(final Set<Dependency> dependencies, final Set<Dependency> dependencyManagement) {
    final Object[] args = new Object[] { dependencies, dependencyManagement };
    final Class<?>[] parameterTypes = new Class<?>[] { Set.class, Set.class };
    return staticMethod("removeDependencyManagment").withReturnType(new TypeRef<Set<Dependency>>() {
      }).withParameterTypes(parameterTypes).in(DisplayDependencyUpdatesMojo.class).invoke(args);
  }

  private void logUpdates(final Map<Dependency, ArtifactVersions> updates, final String section) {
    List<ArtifactUpdate> artiFactUpdates = new ArrayList<ArtifactUpdate>(updates.size());

    for (Entry<Dependency, ArtifactVersions> entry : updates.entrySet()) {
      ArtifactVersions versions = entry.getValue();
      ArtifactVersion latest = null;
      if (versions.isCurrentVersionDefined()) {
        latest = versions.getNewestUpdate(UpdateScope.ANY, Boolean.TRUE.equals(allowSnapshots));
      } else {
        ArtifactVersion newestVersion = versions.getNewestVersion(versions.getArtifact().getVersionRange(), Boolean.TRUE.equals(allowSnapshots));
        if (newestVersion != null) {
          latest = versions.getNewestUpdate(newestVersion, UpdateScope.ANY, Boolean.TRUE.equals(allowSnapshots));
          if (ArtifactVersions.isVersionInRange(latest, versions.getArtifact().getVersionRange())) {
            latest = null;
          }
        }
      }

      if (latest != null) {
        ArtifactUpdate update = new ArtifactUpdate(entry.getKey(), latest);
        artiFactUpdates.add(update);
      }
    }
    updateMap.put(section, artiFactUpdates);
  }
}
