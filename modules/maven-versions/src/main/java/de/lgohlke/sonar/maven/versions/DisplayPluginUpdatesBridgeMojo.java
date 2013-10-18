/*
 * sonar-mojo-bridge-maven-versions
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
package de.lgohlke.sonar.maven.versions;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.lgohlke.sonar.maven.BridgeMojo;
import de.lgohlke.sonar.maven.Goal;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.artifact.metadata.ArtifactMetadataRetrievalException;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.execution.RuntimeInformation;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.interpolation.ModelInterpolationException;
import org.apache.maven.project.interpolation.ModelInterpolator;
import org.codehaus.mojo.versions.DisplayPluginUpdatesMojo;
import org.codehaus.mojo.versions.api.ArtifactVersions;
import org.codehaus.mojo.versions.ordering.MavenVersionComparator;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

import static org.fest.reflect.core.Reflection.field;
import static org.fest.reflect.core.Reflection.method;

@Goal("display-plugin-updates")
@SuppressWarnings("deprecation")
public class DisplayPluginUpdatesBridgeMojo extends DisplayPluginUpdatesMojo implements BridgeMojo<DisplayPluginUpdatesSensor.ResultTransferHandler> {
  @Data
  @RequiredArgsConstructor
  public static class IncompatibleParentAndProjectMavenVersion {
    private final ArtifactVersion parentVersion;
    private final ArtifactVersion projectVersion;
  }

  private final List<ArtifactUpdate> pluginUpdates = Lists.newArrayList();
  private final List<Dependency> missingVersionPlugins = Lists.newArrayList();
  private boolean warninNoMinimumVersion = false;
  private IncompatibleParentAndProjectMavenVersion incompatibleParentAndProjectMavenVersion;

  @Setter
  private DisplayPluginUpdatesSensor.ResultTransferHandler resultHandler;

  @Override
  @SuppressWarnings("unchecked")
  public void execute() throws MojoExecutionException, MojoFailureException {
    subExecute();

    resultHandler.setPluginUpdates(pluginUpdates);
    resultHandler.setMissingVersionPlugins(missingVersionPlugins);
    resultHandler.setWarningNoMinimumVersion(warninNoMinimumVersion);
    resultHandler.setIncompatibleParentAndProjectMavenVersion(incompatibleParentAndProjectMavenVersion);
  }

  private void subExecute() throws MojoExecutionException {
    Set pluginsWithVersionsSpecified;
    try {
      pluginsWithVersionsSpecified = oFindPluginsWithVersionsSpecified(getProject());
    } catch (XMLStreamException e) {
      throw new MojoExecutionException(e.getMessage(), e);
    } catch (IOException e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }

    Map superPomPluginManagement = oGetSuperPomPluginManagement();
    //    System.out.println("superPom plugins = " + superPomPluginManagement);

    Map parentPluginManagement = new HashMap();
    Map parentBuildPlugins = new HashMap();
    Map parentReportPlugins = new HashMap();

    List parents = oGetParentProjects(getProject());

    Iterator i = parents.iterator();
    while (i.hasNext()) {
      MavenProject parentProject = (MavenProject) i.next();
      //      System.out.println("Processing parent: " + parentProject.getGroupId() + ":" + parentProject.getArtifactId() + ":"
      //          + parentProject.getVersion() + " -> " + parentProject.getFile());

      StringWriter writer = new StringWriter();
      boolean havePom = false;
      Model interpolatedModel;
      try {
        Model originalModel = parentProject.getOriginalModel();
        if (originalModel == null) {
          getLog().warn("project.getOriginalModel()==null for  " + parentProject.getGroupId() + ":" +
              parentProject.getArtifactId() + ":" + parentProject.getVersion() +
              " is null, substituting project.getModel()");
          originalModel = parentProject.getModel();
        }
        try {
          new MavenXpp3Writer().write(writer, originalModel);
          writer.close();
          havePom = true;
        } catch (IOException e) {
          // ignore
        }
        interpolatedModel = modelInterpolator().interpolate(originalModel, convertPropertiesToMap());
      } catch (ModelInterpolationException e) {
        throw new MojoExecutionException(e.getMessage(), e);
      }
      if (havePom) {
        try {
          Set withVersionSpecified = oFindPluginsWithVersionsSpecified(new StringBuffer(writer.toString()));
          Map map = oGetPluginManagement(interpolatedModel);
          map.keySet().retainAll(withVersionSpecified);
          parentPluginManagement.putAll(map);

          map = oGetBuildPlugins(interpolatedModel, true);
          map.keySet().retainAll(withVersionSpecified);
          parentPluginManagement.putAll(map);

          map = oGetReportPlugins(interpolatedModel, true);
          map.keySet().retainAll(withVersionSpecified);
          parentPluginManagement.putAll(map);
        } catch (IOException e) {
          throw new MojoExecutionException(e.getMessage(), e);
        } catch (XMLStreamException e) {
          throw new MojoExecutionException(e.getMessage(), e);
        }
      } else {
        parentPluginManagement.putAll(oGetPluginManagement(interpolatedModel));
        parentPluginManagement.putAll(oGetBuildPlugins(interpolatedModel, true));
        parentPluginManagement.putAll(oGetReportPlugins(interpolatedModel, true));
      }
    }

    Set plugins = oGetProjectPlugins(superPomPluginManagement, parentPluginManagement, parentBuildPlugins,
        parentReportPlugins, pluginsWithVersionsSpecified);

    //    List updates = new ArrayList();
    //    List lockdown = new ArrayList();
    Map /*<ArtifactVersion,Map<String,String>>*/ upgrades = new TreeMap(new MavenVersionComparator());
    ArtifactVersion curMavenVersion = runtimeInformation().getApplicationVersion();
    ArtifactVersion specMavenVersion = new DefaultArtifactVersion(oGetRequiredMavenVersion(getProject(), "2.0"));
    ArtifactVersion minMavenVersion = null;

    //    boolean superPomDrivingMinVersion = false;
    i = plugins.iterator();
    while (i.hasNext()) {
      Object plugin = i.next();
      String groupId = oGetPluginGroupId(plugin);
      String artifactId = oGetPluginArtifactId(plugin);
      String version = oGetPluginVersion(plugin);
      String coords = ArtifactUtils.versionlessKey(groupId, artifactId);

      if (version == null) {
        version = (String) parentPluginManagement.get(coords);
      }

      //      System.out.println(
      //          new StringBuffer().append("Checking ").append(coords).append(" for updates newer than ").append(
      //              version).toString());
      String effectiveVersion = version;

      VersionRange versionRange;
      boolean unspecified = version == null;
      try {
        versionRange = unspecified ? VersionRange.createFromVersionSpec("[0,)") : VersionRange.createFromVersionSpec(version);
      } catch (InvalidVersionSpecificationException e) {
        throw new MojoExecutionException("Invalid version range specification: " + version, e);
      }

      Artifact artifact = artifactFactory.createPluginArtifact(groupId, artifactId, versionRange);

      ArtifactVersion artifactVersion = null;
      try {
        // now we want to find the newest version that is compatible with the invoking version of Maven
        ArtifactVersions artifactVersions = getHelper().lookupArtifactVersions(artifact, true);
        ArtifactVersion[] newerVersions = artifactVersions.getVersions(Boolean.TRUE.equals(this.allowSnapshots));
        ArtifactVersion minRequires = null;
        for (int j = newerVersions.length - 1; j >= 0; j--) {
          Artifact probe = artifactFactory.createDependencyArtifact(groupId, artifactId,
              VersionRange.createFromVersion(newerVersions[j].toString()), "pom", null, "runtime");
          try {
            getHelper().resolveArtifact(probe, true);

            MavenProject mavenProject = projectBuilder.buildFromRepository(probe, remotePluginRepositories,
                localRepository);
            ArtifactVersion requires = new DefaultArtifactVersion(oGetRequiredMavenVersion(mavenProject, "2.0"));
            if ((specMavenVersion.compareTo(requires) >= 0) && (artifactVersion == null)) {
              artifactVersion = newerVersions[j];
            }
            if ((effectiveVersion == null) && (curMavenVersion.compareTo(requires) >= 0)) {
              // version was unspecified, current version of maven thinks it should use this
              effectiveVersion = newerVersions[j].toString();
            }
            if ((artifactVersion != null) && (effectiveVersion != null)) {
              // no need to look at any older versions.
              break;
            }
            if ((minRequires == null) || (minRequires.compareTo(requires) > 0)) {
              Map /*<String,String*/ upgradePlugins = (Map) upgrades.get(requires);
              if (upgradePlugins == null) {
                upgrades.put(requires, upgradePlugins = new LinkedHashMap());
              }

              String upgradePluginKey = oCompactKey(groupId, artifactId);
              if (!upgradePlugins.containsKey(upgradePluginKey)) {
                upgradePlugins.put(upgradePluginKey, newerVersions[j].toString());
              }
              minRequires = requires;
            }
          } catch (ArtifactResolutionException e) {
            // ignore bad version
          } catch (ArtifactNotFoundException e) {
            // ignore bad version
          } catch (ProjectBuildingException e) {
            // ignore bad version
          }
        }
        if (effectiveVersion != null) {
          VersionRange currentVersionRange = VersionRange.createFromVersion(effectiveVersion);
          Artifact probe = artifactFactory.createDependencyArtifact(groupId, artifactId, currentVersionRange, "pom",
              null,
              "runtime");
          try {
            getHelper().resolveArtifact(probe, true);

            MavenProject mavenProject = projectBuilder.buildFromRepository(probe, remotePluginRepositories,
                localRepository);
            ArtifactVersion requires = new DefaultArtifactVersion(oGetRequiredMavenVersion(mavenProject, "2.0"));
            if ((minMavenVersion == null) || (minMavenVersion.compareTo(requires) < 0)) {
              minMavenVersion = requires;
            }
          } catch (ArtifactResolutionException e) {
            // ignore bad version
          } catch (ArtifactNotFoundException e) {
            // ignore bad version
          } catch (ProjectBuildingException e) {
            // ignore bad version
          }
        }
      } catch (ArtifactMetadataRetrievalException e) {
        throw new MojoExecutionException(e.getMessage(), e);
      }

      String newVersion;

      if ((version == null) && pluginsWithVersionsSpecified.contains(coords)) {
        // Hack ALERT!
        //
        // All this should be re-written in a less "pom is xml" way... but it'll
        // work for now :-(
        //
        // we have removed the version information, as it was the same as from
        // the super-pom... but it actually was specified.
        version = (artifactVersion != null) ? artifactVersion.toString() : null;
      }

      //      System.out.println("[" + coords + "].version=" + version);
      //      System.out.println("[" + coords + "].artifactVersion=" + artifactVersion);
      //      System.out.println("[" + coords + "].effectiveVersion=" + effectiveVersion);
      //      System.out.println("[" + coords + "].specified=" + pluginsWithVersionsSpecified.contains(coords));
      if ((version == null) || !pluginsWithVersionsSpecified.contains(coords)) {
        version = (String) superPomPluginManagement.get(ArtifactUtils.versionlessKey(artifact));
        //        System.out.println("[" + coords + "].superPom.version=" + version);

        newVersion = (artifactVersion != null) ? artifactVersion.toString()
            : ((version != null) ? version : ((effectiveVersion != null) ? effectiveVersion : "(unknown)"));

        //        StringBuffer buf = new StringBuffer(oCompactKey(groupId, artifactId));
        //        buf.append(' ');
        //        int padding =
        //            WARN_PAD_SIZE() - effectiveVersion.length() - (version != null ? FROM_SUPER_POM().length() : 0);
        //        while (buf.length() < padding) {
        //          buf.append('.');
        //        }
        //        buf.append(' ');
        //        if (version != null) {
        //          buf.append(FROM_SUPER_POM());
        //          superPomDrivingMinVersion = true;
        //        }
        //        buf.append(effectiveVersion);
        //        lockdown.add(buf.toString());
        addMissingVersionPlugin(groupId, artifactId, version);
      } else if (artifactVersion != null) {
        newVersion = artifactVersion.toString();
      } else {
        newVersion = null;
      }
      if ((version != null) && (artifactVersion != null) && (newVersion != null) &&
          (new DefaultArtifactVersion(effectiveVersion).compareTo(new DefaultArtifactVersion(newVersion)) < 0)) {
        addUpdate(groupId, artifactId, version, artifactVersion);
      }
    }

    boolean noMavenMinVersion = oGetRequiredMavenVersion(getProject(), null) == null;
    boolean noExplicitMavenMinVersion = (getProject().getPrerequisites() == null) ||
        (getProject().getPrerequisites().getMaven() == null);
    if (noMavenMinVersion) {
      warninNoMinimumVersion = true;
    } else if (noExplicitMavenMinVersion) {
      //      getLog().info( "Project inherits minimum Maven version as: " + specMavenVersion );
    } else {
      ArtifactVersion explicitMavenVersion = new DefaultArtifactVersion(getProject().getPrerequisites().getMaven());
      if (explicitMavenVersion.compareTo(specMavenVersion) < 0) {
        incompatibleParentAndProjectMavenVersion = new IncompatibleParentAndProjectMavenVersion(specMavenVersion,
            explicitMavenVersion);
      }
    }

    //    getLog().info("Plugins require minimum Maven version of: " + minMavenVersion);
    //    if (superPomDrivingMinVersion) {
    //      getLog().info("Note: the super-pom from Maven " + curMavenVersion + " defines some of the plugin");
    //      getLog().info("      versions and may be influencing the plugins required minimum Maven");
    //      getLog().info("      version.");
    //    }
    if ("maven-plugin".equals(getProject().getPackaging())) {
      //      if (noMavenMinVersion) {
      //        getLog().warn("Project (which is a Maven Plugin) does not define required minimum version of Maven.");
      //        getLog().warn("Update the pom.xml to contain");
      //        getLog().warn("    <prerequisites>");
      //        getLog().warn("      <maven><!-- minimum version of Maven that the plugin works with --></maven>");
      //        getLog().warn("    </prerequisites>");
      //        getLog().warn("To build this plugin you need at least Maven " + minMavenVersion);
      //        getLog().warn("A Maven Enforcer rule can be used to enforce this if you have not already set one up");
      //      } else if (minMavenVersion != null && specMavenVersion.compareTo(minMavenVersion) < 0) {
      //        getLog().warn("Project (which is a Maven Plugin) targets Maven " + specMavenVersion + " or newer");
      //        getLog().warn("but requires Maven " + minMavenVersion + " or newer to build.");
      //        getLog().warn("This may or may not be a problem. A Maven Enforcer rule can help ");
      //        getLog().warn("enforce that the correct version of Maven is used to build this plugin.");
      //      } else {
      //        getLog().info("No plugins require a newer version of Maven than specified by the pom.");
      //      }
    } else {
      if (noMavenMinVersion) {
        noMavenMinVersion = true;
        //        getLog/*().error("Project does not define required minimum version of Maven.");
        //        getLog().error("Update the pom.xml to contain");
        //        getLog().error("    <prerequisites>");
        //        getLog().error("      <maven>" + minMavenVersion + "</maven>");
        //        getLog(*/).error("    </prerequisites>");
      } else if ((minMavenVersion != null) && (specMavenVersion.compareTo(minMavenVersion) < 0)) {
        incompatibleParentAndProjectMavenVersion = new IncompatibleParentAndProjectMavenVersion(specMavenVersion,
            minMavenVersion);
        //        getLog().error("Project requires an incorrect minimum version of Maven.");
        //        getLog().error("Either change plugin versions to those compatible with " + specMavenVersion);
        //        getLog().error("or update the pom.xml to contain");
        //        getLog().error("    <prerequisites>");
        //        getLog().error("      <maven>" + minMavenVersion + "</maven>");
        //        getLog().error("    </prerequisites>");
      }
      //    i = upgrades.entrySet().iterator();
      //    while (i.hasNext()) {
      //      Map.Entry mavenUpgrade = (Map.Entry) i.next();
      //      ArtifactVersion mavenUpgradeVersion = (ArtifactVersion) mavenUpgrade.getKey();
      //      Map upgradePlugins = (Map) mavenUpgrade.getValue();
      //      if (upgradePlugins.isEmpty() || specMavenVersion.compareTo(mavenUpgradeVersion) >= 0) {
      //        continue;
      //      }
      //      getLog().info("");
      //      getLog().info("Require Maven " + mavenUpgradeVersion + " to use the following plugin updates:");
      //      for (Iterator j = upgradePlugins.entrySet().iterator(); j.hasNext(); ) {
      //        Map.Entry entry = (Map.Entry) j.next();
      //        StringBuffer buf = new StringBuffer("  ");
      //        buf.append(entry.getKey().toString());
      //        buf.append(' ');
      //        String s = entry.getValue().toString();
      //        int padding = INFO_PAD_SIZE() - s.length() + 2;
      //        while (buf.length() < padding) {
      //          buf.append('.');
      //        }
      //        buf.append(' ');
      //        buf.append(s);
      //        getLog().info(buf.toString());
      //      }
      //    }
      //    getLog().info("");
    }
  }

  private void addMissingVersionPlugin(final String groupId, final String artifactId, final String version) {
    Dependency dependency = DependencyUtils.createDependency(groupId, artifactId, version);
    missingVersionPlugins.add(dependency);
  }

  private void addUpdate(final String groupId, final String artifactId, final String version,
                         final ArtifactVersion artifactVersion) {
    Dependency dependency = DependencyUtils.createDependency(groupId, artifactId, version);
    ArtifactUpdate update = new ArtifactUpdate(dependency, artifactVersion);
    pluginUpdates.add(update);
  }

  private Map<String, ?> convertPropertiesToMap() {
    Properties properties = getProject().getProperties();
    return Maps.fromProperties(properties);
  }

  private RuntimeInformation runtimeInformation() {
    return field("runtimeInformation").ofType(RuntimeInformation.class).in(this).get();
  }

  private ModelInterpolator modelInterpolator() {
    return field("modelInterpolator").ofType(ModelInterpolator.class).in(this).get();
  }

  private String oCompactKey(String groupId, String artifactId) {
    return method("compactKey").withReturnType(String.class).withParameterTypes(String.class, String.class).in(this).invoke(groupId, artifactId);
  }

  private String oGetRequiredMavenVersion(MavenProject mavenProject, String defaultValue) {
    return method("getRequiredMavenVersion").withReturnType(String.class)
        .withParameterTypes(MavenProject.class, String.class)
        .in(this)
        .invoke(mavenProject, defaultValue);
  }

  private Set oFindPluginsWithVersionsSpecified(MavenProject project) throws IOException, XMLStreamException {
    return method("findPluginsWithVersionsSpecified").withReturnType(Set.class).withParameterTypes(MavenProject.class).in(this).invoke(project);
  }

  private Set oFindPluginsWithVersionsSpecified(StringBuffer pomContents) throws IOException, XMLStreamException {
    return method("findPluginsWithVersionsSpecified").withReturnType(Set.class).withParameterTypes(StringBuffer.class).in(this).invoke(pomContents);
  }

  private List oGetParentProjects(MavenProject project) throws MojoExecutionException {
    return method("getParentProjects").withReturnType(List.class).withParameterTypes(MavenProject.class).in(this).invoke(project);
  }

  private Map oGetSuperPomPluginManagement() throws MojoExecutionException {
    return method("getSuperPomPluginManagement").withReturnType(Map.class).in(this).invoke();
  }

  private Map oGetPluginManagement(Model model) {
    return method("getPluginManagement").withReturnType(Map.class).withParameterTypes(Model.class).in(this).invoke(model);
  }

  private Map oGetBuildPlugins(Model model, boolean onlyIncludeInherited) {
    return method("getBuildPlugins").withReturnType(Map.class)
        .withParameterTypes(Model.class, boolean.class)
        .in(this)
        .invoke(model, onlyIncludeInherited);

  }

  private Map oGetReportPlugins(Model model, boolean onlyIncludeInherited) {
    return method("getReportPlugins").withReturnType(Map.class)
        .withParameterTypes(Model.class, boolean.class)
        .in(this)
        .invoke(model, onlyIncludeInherited);
  }

  private Set oGetProjectPlugins(Map superPomPluginManagement, Map parentPluginManagement, Map parentBuildPlugins,
                                 Map parentReportPlugins, Set pluginsWithVersionsSpecified) throws MojoExecutionException {
    return method("getProjectPlugins").withReturnType(Set.class)
        .withParameterTypes(Map.class, Map.class, Map.class, Map.class, Set.class)
        .in(this)
        .invoke(superPomPluginManagement, parentPluginManagement, parentBuildPlugins, parentReportPlugins,
            pluginsWithVersionsSpecified);
  }

  private static String oGetPluginArtifactId(Object plugin) {
    return method("getPluginArtifactId").withReturnType(String.class)
        .withParameterTypes(Object.class)
        .in(DisplayPluginUpdatesMojo.class)
        .invoke(plugin);
  }

  private static String oGetPluginGroupId(Object plugin) {
    return method("getPluginGroupId").withReturnType(String.class).withParameterTypes(Object.class).in(DisplayPluginUpdatesMojo.class).invoke(plugin);
  }

  private static String oGetPluginVersion(Object plugin) {
    return method("getPluginVersion").withReturnType(String.class).withParameterTypes(Object.class).in(DisplayPluginUpdatesMojo.class).invoke(plugin);
  }
}
