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

import de.lgohlke.sonar.maven.BridgeMojoMapper;
import de.lgohlke.sonar.maven.versions.UpdateParentPomSensor;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.model.InputLocation;
import org.apache.maven.model.Parent;
import org.apache.maven.project.MavenProject;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.Violation;
import org.testng.annotations.Test;

import java.io.File;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * User: lars
 */
public class UpdateParentPomSensorTest {
    @Test
    public void analyseWithNulNewVersion() {
        UpdateParentPomSensor sensor = initSensor();
        initBridgeMojoMapper(sensor, null, null);

        SensorContext mockContext = mock(SensorContext.class);
        sensor.analyse(mock(Project.class), mockContext);

        verify(mockContext, times(0)).saveViolation(any(Violation.class));
    }

    @Test
    public void analyseWithNewVersion() {
        UpdateParentPomSensor sensor = initSensor();
        initBridgeMojoMapper(sensor, new DefaultArtifactVersion("1.0"), "1.1");

        Parent parent = new Parent();
        parent.setLocation("version", new InputLocation(1, 1));
        MavenProject mavenProject = new MavenProject();
        mavenProject.getModel().setParent(parent);
        mavenProject.setFile(new File("pom.xml"));
        when(sensor.getMavenProject()).thenReturn(mavenProject);

        SensorContext mockContext = mock(SensorContext.class);
        sensor.analyse(mock(Project.class), mockContext);

        verify(mockContext, times(1)).saveViolation(any(Violation.class));
    }

    private UpdateParentPomSensor initSensor() {
        UpdateParentPomSensor sensor = mock(UpdateParentPomSensor.class);
        doCallRealMethod().when(sensor).analyse(any(Project.class), any(SensorContext.class));
        return sensor;
    }

    private void initBridgeMojoMapper(UpdateParentPomSensor mock, ArtifactVersion newerVersion, String currentVersion) {
        BridgeMojoMapper<UpdateParentPomSensor.ResultHandler> mojoMapper = mock(BridgeMojoMapper.class);
        when(mock.getMojoMapper()).thenReturn(mojoMapper);
        UpdateParentPomSensor.ResultHandler resulHandler = new UpdateParentPomSensor.ResultHandler();
        resulHandler.setNewerVersion(newerVersion);
        resulHandler.setCurrentVersion(currentVersion);
        when(mojoMapper.getResultTransferHandler()).thenReturn(resulHandler);
    }
}
