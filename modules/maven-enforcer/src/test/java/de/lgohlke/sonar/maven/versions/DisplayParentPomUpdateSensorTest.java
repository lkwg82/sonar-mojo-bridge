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
import org.apache.maven.model.InputLocation;
import org.apache.maven.model.Parent;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.versions.report.ArtifactUpdate;
import org.codehaus.mojo.versions.report.Dependency;
import org.codehaus.mojo.versions.report.DisplayParentUpdateReport;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sonar.api.batch.maven.MavenPluginHandler;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issue;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rule.RuleKey;
import org.sonar.core.issue.DefaultIssueBuilder;
import org.testng.annotations.Test;
import versions.DisplayParentPomUpdateSensor;
import versions.rules.ParentPomVersion;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class DisplayParentPomUpdateSensorTest {
    private final List<Issue> issues = new ArrayList<Issue>();

    class MyDisplayParentPomUpdateSensor extends DisplayParentPomUpdateSensor {
        @Setter
        private DisplayParentUpdateReport report;

        public MyDisplayParentPomUpdateSensor(RulesProfile rulesProfile, MavenProject mavenProject, ResourcePerspectives resourcePerspectives, Settings settings) {
            super(rulesProfile, mavenProject, resourcePerspectives, settings);
        }

        @Override
        protected <T> T getXmlAsFromReport(String pathToXmlReport, Class<T> clazz) {
            return (T) report;
        }
    }

    @Test
    public void analyseWithNullNoNewVersion() {

        Parent parent = new Parent();
        parent.setLocation("version", new InputLocation(1, 1));
        MavenProject mavenProject = new MavenProject();
        mavenProject.getModel().setParent(parent);
        mavenProject.setFile(new File("pom.xml"));

        MyDisplayParentPomUpdateSensor sensor = initSensor(mavenProject);

        issues.clear();

        DisplayParentUpdateReport report = createReport(null);

        sensor.setReport(report);

        sensor.analyse(null, null);

        assertThat(issues).isEmpty();
    }

    @Test
    public void analyseWithNewVersion() {
        issues.clear();

        Parent parent = new Parent();
        parent.setLocation("version", new InputLocation(1, 1));
        MavenProject mavenProject = new MavenProject();
        mavenProject.getModel().setParent(parent);
        mavenProject.setFile(new File("pom.xml"));

        MyDisplayParentPomUpdateSensor sensor = initSensor(mavenProject);

        DisplayParentUpdateReport report = createReport("1");

        sensor.setReport(report);

        sensor.analyse(null, null);

        assertThat(issues).hasSize(1);
        assertThat(issues.get(0).line()).isEqualTo(1);
        assertThat(issues.get(0).ruleKey()).isEqualTo(RuleKey.of("maven", ParentPomVersion.KEY));
    }

    private DisplayParentUpdateReport createReport(String versionUpdate) {
        Dependency dependency = new Dependency();
        dependency.setGroupId("group");
        dependency.setArtifactId("artifact");
        dependency.setVersion("1");

        ArtifactUpdate update = new ArtifactUpdate();
        update.setDependency(dependency);
        update.setVersionUpdate(versionUpdate);

        DisplayParentUpdateReport report = new DisplayParentUpdateReport();
        report.setUpdate(update);
        return report;
    }

    @Test
    public void testAnalyseWithNoParentPom() {
        issues.clear();

        Parent parent = new Parent();
        parent.setLocation("version", new InputLocation(1, 1));
        MavenProject mavenProject = new MavenProject();
        mavenProject.getModel().setParent(null);
        mavenProject.setFile(new File("pom.xml"));
        MyDisplayParentPomUpdateSensor sensor = initSensor(mavenProject);

        sensor.analyse(null, null);

        assertThat(issues).isEmpty();
    }

    @Test
    public void testMavenHandler() {
        final MavenProject mavenProject = new MavenProject();
        DisplayParentPomUpdateSensor sensor = initSensor(mavenProject);
        MavenPluginHandler mavenPluginHandler = sensor.getMavenPluginHandler(mock(Project.class));

        assertThat(mavenPluginHandler.getGoals()).hasSize(1);
        assertThat(mavenPluginHandler.getGoals()).contains("display-parent-update");
        assertThat(mavenProject.getProperties()).containsKey("xmlReport");
    }

    private MyDisplayParentPomUpdateSensor initSensor(MavenProject mavenProject) {
        RulesProfile rulesProfile = mock(RulesProfile.class);

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
        return new MyDisplayParentPomUpdateSensor(rulesProfile, mavenProject, resourcePerspectives, settings);
    }
}
