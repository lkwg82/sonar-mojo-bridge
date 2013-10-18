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
import de.lgohlke.sonar.maven.versions.rules.DependencyVersion;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.InputLocation;
import org.apache.maven.model.InputSource;
import org.apache.maven.project.MavenProject;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issue;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.File;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RulePriority;
import org.sonar.batch.scan.maven.MavenPluginExecutor;
import org.sonar.core.issue.DefaultIssueBuilder;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * User: lgohlke
 */
public class DisplayDependencyUpdatesSensorTest {
  @Test
  public void shouldAnalyse() throws Exception {
    RulesProfile rulesProfile = prepareRulesProfile();
    MavenProject mavenProject = TestHelper.getMavenProject();
    Settings settings = Settings.createForComponent(DisplayDependencyUpdatesSensor.class);

    final List<Issue> issues = new ArrayList<Issue>();

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
    when(resourcePerspectives.as(eq(Issuable.class), any(File.class))).thenReturn(issuable);

    DisplayDependencyUpdatesSensor sensor = new DisplayDependencyUpdatesSensor(rulesProfile, mock(MavenPluginExecutor.class), mavenProject, settings, resourcePerspectives);

    String effectiveKey = "a";
    String analysisVersion = "1";
    String artifactQualifier = "group:artifact:version:goal";

    Map<String, List<ArtifactUpdate>> updateMap = prepareUpdateMap(effectiveKey, analysisVersion, artifactQualifier);
    sensor.getMojoMapper().getResultTransferHandler().setUpdateMap(updateMap);

    Project project = new Project(effectiveKey).setAnalysisVersion(analysisVersion);

    sensor.analyse(project, null);

    assertThat(issues).hasSize(1);
    assertThat(issues.get(0).message()).contains(artifactQualifier);
  }

  private Map<String, List<ArtifactUpdate>> prepareUpdateMap(String effectiveKey, String analysisVersion, String artifactQualifier) {
    Map<String, List<ArtifactUpdate>> updateMap = Maps.newHashMap();
    ArtifactUpdate artifactUpdate = mock(ArtifactUpdate.class);
    InputSource inputSource = new InputSource();
    inputSource.setModelId(effectiveKey + ":" + analysisVersion);
    InputLocation inputLocation = new InputLocation(1, 1, inputSource);

    Dependency mockDependency = mock(Dependency.class);
    when(mockDependency.getLocation("version")).thenReturn(new InputLocation(1, 1));
    when(mockDependency.getLocation("")).thenReturn(inputLocation);

    when(artifactUpdate.getDependency()).thenReturn(mockDependency);

    ArtifactVersion mockArtifactVersion = mock(ArtifactVersion.class);
    when(mockArtifactVersion.toString()).thenReturn(artifactQualifier);
    when(artifactUpdate.getArtifactVersion()).thenReturn(mockArtifactVersion);

    List<ArtifactUpdate> updateList = Lists.newArrayList(artifactUpdate);
    when(updateList.get(0).toString()).thenReturn(artifactQualifier);
    updateMap.put(DisplayDependencyUpdatesBridgeMojo.DEPENDENCIES, updateList);
    return updateMap;
  }

  private RulesProfile prepareRulesProfile() {
    RulesProfile rulesProfile = RulesProfile.create("mine", "java");
    Rule rule = Rule.create(de.lgohlke.sonar.Configuration.REPOSITORY_KEY, DependencyVersion.KEY, DependencyVersion.NAME);
    rule.createParameter(DependencyVersion.RULE_PROPERTY_WHITELIST);
    rule.createParameter(DependencyVersion.RULE_PROPERTY_BLACKLIST);

    ActiveRule activeRule = rulesProfile.activateRule(rule, RulePriority.MAJOR);
    activeRule.setParameter(DependencyVersion.RULE_PROPERTY_WHITELIST, ".*");
    activeRule.setParameter(DependencyVersion.RULE_PROPERTY_BLACKLIST, "");
    return rulesProfile;
  }
}
