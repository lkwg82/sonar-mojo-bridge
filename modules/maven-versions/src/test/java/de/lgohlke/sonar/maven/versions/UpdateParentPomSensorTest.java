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
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.model.InputLocation;
import org.apache.maven.model.Parent;
import org.apache.maven.project.MavenProject;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issue;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.batch.scan.maven.MavenPluginExecutor;
import org.sonar.core.issue.DefaultIssueBuilder;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class UpdateParentPomSensorTest {
  private final List<Issue> issues = new ArrayList<Issue>();
  private UpdateParentPomSensor sensor = initSensor();

  @Test
  public void analyseWithNullNewVersion() {
    initBridgeMojoMapper(sensor, null, null);

    issues.clear();
    sensor.analyse(null, null);

    assertThat(issues).isEmpty();
  }

  @Test
  public void analyseWithNewVersion() {
    initBridgeMojoMapper(sensor, new DefaultArtifactVersion("1.0"), "1.1");
    issues.clear();

    Parent parent = new Parent();
    parent.setLocation("version", new InputLocation(1, 1));
    MavenProject mavenProject = new MavenProject();
    mavenProject.getModel().setParent(parent);
    mavenProject.setFile(new File("pom.xml"));
    when(sensor.getMavenProject()).thenReturn(mavenProject);

    sensor.analyse(null, null);

    assertThat(issues).hasSize(1);
  }

  private UpdateParentPomSensor initSensor() {
    RulesProfile rulesProfile = mock(RulesProfile.class);
    MavenPluginExecutor mavenPluginExecutor = mock(MavenPluginExecutor.class);
    MavenProject mavenProject = mock(MavenProject.class);

    Issuable issuable = mock(Issuable.class);
    when(issuable.addIssue(any(Issue.class))).thenAnswer(new Answer() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        Issue issue = (Issue) invocation.getArguments()[0];
        issues.add(issue);
        return null;
      }
    });
    when(issuable.newIssueBuilder()).thenReturn(new DefaultIssueBuilder().componentKey("xxx"));
    ResourcePerspectives resourcePerspectives = mock(ResourcePerspectives.class);
    when(resourcePerspectives.as(eq(Issuable.class), any(org.sonar.api.resources.File.class))).thenReturn(issuable);

    Settings settings = mock(Settings.class);
    return spy(new UpdateParentPomSensor(rulesProfile, mavenPluginExecutor, mavenProject, resourcePerspectives, settings));
  }

  private void initBridgeMojoMapper(UpdateParentPomSensor sensor, ArtifactVersion newerVersion, String currentVersion) {
    @SuppressWarnings("unchecked")
    BridgeMojoMapper<UpdateParentPomSensor.ResultHandler> mojoMapper = mock(BridgeMojoMapper.class);
    sensor.setMojoMapper(mojoMapper);

    UpdateParentPomSensor.ResultHandler resulHandler = new UpdateParentPomSensor.ResultHandler();
    resulHandler.setNewerVersion(newerVersion);
    resulHandler.setCurrentVersion(currentVersion);
    when(mojoMapper.getResultTransferHandler()).thenReturn(resulHandler);
  }
}
