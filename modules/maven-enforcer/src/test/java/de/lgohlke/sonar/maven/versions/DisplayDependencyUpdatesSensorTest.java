/*
 * sonar-mojo-bridge-maven-enforcer
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

import lombok.Setter;
import org.apache.maven.model.InputSource;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.versions.report.ArtifactUpdate;
import org.codehaus.mojo.versions.report.Dependency;
import org.codehaus.mojo.versions.report.DisplayDependencyUpdatesReport;
import org.codehaus.mojo.versions.report.InputLocation;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sonar.api.batch.maven.MavenPluginHandler;
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
import org.sonar.core.issue.DefaultIssueBuilder;
import org.testng.annotations.Test;
import versions.DisplayDependencyUpdatesSensor;
import versions.rules.DependencyVersion;

import java.util.ArrayList;
import java.util.HashMap;
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

    private class MyDisplayDependencyUpdatesSensor extends DisplayDependencyUpdatesSensor {
        @Setter
        private DisplayDependencyUpdatesReport report;

        public MyDisplayDependencyUpdatesSensor(RulesProfile rulesProfile, MavenProject mavenProject, Settings settings, ResourcePerspectives resourcePerspectives) {
            super(rulesProfile, mavenProject, settings, resourcePerspectives);
        }

        @Override
        protected <T>T getXmlAsFromReport(String pathToXmlReport, Class<T> clazz) {
            return (T) report;
        }
    }

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

        MyDisplayDependencyUpdatesSensor sensor = new MyDisplayDependencyUpdatesSensor(rulesProfile, mavenProject, settings, resourcePerspectives);

        String effectiveKey = "a";
        String analysisVersion = "1";
        String artifactQualifier = "group:artifact:version:goal";

        sensor.setReport(createReport(effectiveKey, analysisVersion, artifactQualifier));

        Project project = new Project(effectiveKey).setAnalysisVersion(analysisVersion);

        sensor.analyse(project, null);

        assertThat(issues).hasSize(1);
        assertThat(issues.get(0).message()).contains(artifactQualifier);
        assertThat(issues.get(0).line()).isEqualTo(1);
    }

    private DisplayDependencyUpdatesReport createReport(String effectiveKey, String analysisVersion, String artifactQualifier) {

        final Dependency dependency = new Dependency();

        final ArtifactUpdate update = new ArtifactUpdate();
        update.setDependency(dependency);
        final InputLocation inputLocation = new InputLocation();
        inputLocation.setLine(1);
        inputLocation.setColumn(1);
        final InputSource inputSource = new InputSource();
        inputSource.setModelId(effectiveKey + ":" + analysisVersion);
        inputLocation.setInputSource(inputSource);
        dependency.getInputLocationMap().put("version", inputLocation);
        dependency.getInputLocationMap().put("", inputLocation);

        update.setVersionUpdate(artifactQualifier);

        ArrayList<ArtifactUpdate> artifactUpdates = new ArrayList<ArtifactUpdate>();
        artifactUpdates.add(update);

        Map<String, List<ArtifactUpdate>> updateMap = new HashMap<String, List<ArtifactUpdate>>();
        updateMap.put("Dependencies", artifactUpdates);

        DisplayDependencyUpdatesReport report = new DisplayDependencyUpdatesReport();
        report.getUpdatePerSectionMap().putAll(updateMap);
        return report;
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

    @Test
    public void testMavenHandler() {
        final MavenProject mavenProject = new MavenProject();
        DisplayDependencyUpdatesSensor sensor = new DisplayDependencyUpdatesSensor(RulesProfile.create(), mavenProject, new Settings(), mock(ResourcePerspectives.class));

        MavenPluginHandler mavenPluginHandler = sensor.getMavenPluginHandler(mock(Project.class));

        assertThat(mavenPluginHandler.getGoals()).hasSize(1);
        assertThat(mavenPluginHandler.getGoals()).contains("display-dependency-updates");
        assertThat(mavenProject.getProperties()).containsKey("xmlReport");
    }
}
