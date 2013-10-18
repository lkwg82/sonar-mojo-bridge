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

import de.lgohlke.sonar.maven.BridgeMojo;
import de.lgohlke.sonar.maven.Goal;
import lombok.Setter;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadataRetrievalException;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.mojo.versions.UpdateParentMojo;
import org.codehaus.mojo.versions.rewriting.ModifiedPomXMLEventReader;


@Goal("update-parent")
@SuppressWarnings("deprecation")
public class UpdateParentBridgeMojo extends UpdateParentMojo implements BridgeMojo<UpdateParentPomSensor.ResultHandler> {
    @Setter
    private UpdateParentPomSensor.ResultHandler resultHandler;

    @Override
    protected void update(ModifiedPomXMLEventReader pom) throws MojoExecutionException, MojoFailureException {
        if (hasParentPom() && !isPartOfReactorProject()) {
            String currentVersion = getProject().getParent().getVersion();
            String version = currentVersion;

            if (parentVersion != null) {
                version = parentVersion;
            }

            VersionRange versionRange;
            try {
                versionRange = VersionRange.createFromVersionSpec(version);
            } catch (InvalidVersionSpecificationException e) {
                throw new MojoExecutionException("Invalid version range specification: " + version, e);
            }

            Artifact artifact = artifactFactory.createDependencyArtifact(getProject().getParent().getGroupId(),
                    getProject().getParent().getArtifactId(),
                    versionRange, "pom", null, null);

            ArtifactVersion artifactVersion;
            try {
                artifactVersion = findLatestVersion(artifact, versionRange, null, false);
            } catch (ArtifactMetadataRetrievalException e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }

            if (!shouldApplyUpdate(artifact, currentVersion, artifactVersion)) {
                return;
            }

            resultHandler.setCurrentVersion(currentVersion);
            resultHandler.setNewerVersion(artifactVersion);
        }
    }

    private boolean isPartOfReactorProject() {
        return reactorProjects.contains(getProject().getParent());
    }

    private boolean hasParentPom() {
        return getProject().getParent() != null;
    }
}
