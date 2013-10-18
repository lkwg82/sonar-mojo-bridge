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
import de.lgohlke.sonar.PomSourceImporter;
import de.lgohlke.sonar.maven.MavenRule;
import de.lgohlke.sonar.maven.versions.rules.IncompatibleMavenVersion;
import de.lgohlke.sonar.maven.versions.rules.MissingPluginVersion;
import de.lgohlke.sonar.maven.versions.rules.NoMinimumMavenVersion;
import de.lgohlke.sonar.maven.versions.rules.PluginVersion;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.InputLocation;
import org.apache.maven.project.MavenProject;
import org.fest.assertions.core.Condition;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issue;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.File;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RulePriority;
import org.sonar.batch.scan.maven.MavenPluginExecutor;
import org.sonar.core.issue.DefaultIssueBuilder;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DisplayPluginUpdatesSensorTest {
  private DisplayPluginUpdatesSensor.ResultTransferHandler resultTransferHandler;
  private List<Issue> issues;
  private DisplayPluginUpdatesSensor sensor;

  //  @Before
  public void init() {
    issues = new ArrayList<Issue>();

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

    sensor = getPluginUpdatesSensor(resourcePerspectives);

    resultTransferHandler = sensor.getMojoMapper().getResultTransferHandler();
    resultTransferHandler.setMissingVersionPlugins(new ArrayList<Dependency>());
    resultTransferHandler.setIncompatibleParentAndProjectMavenVersion(null);
    resultTransferHandler.setPluginUpdates(new ArrayList<ArtifactUpdate>());
    resultTransferHandler.setWarningNoMinimumVersion(false);
  }

  private DisplayPluginUpdatesSensor getPluginUpdatesSensor(ResourcePerspectives resourcePerspectives) {
    RulesProfile rulesProfile = RulesProfile.create("mine", "java");
    Rule rule = Rule.create(de.lgohlke.sonar.Configuration.REPOSITORY_KEY, PluginVersion.KEY, PluginVersion.NAME);
    rule.createParameter(PluginVersion.RULE_PROPERTY_WHITELIST).setDefaultValue(".*");
    rule.createParameter(PluginVersion.RULE_PROPERTY_BLACKLIST).setDefaultValue("");
    rulesProfile.activateRule(rule, RulePriority.MAJOR);

    MavenProject mavenProject = TestHelper.getMavenProject();
    Settings settings = Settings.createForComponent(DisplayPluginUpdatesSensor.class);
    PomSourceImporter pomSourceImporter = TestHelper.getPomSourceImporter();

    return new DisplayPluginUpdatesSensor(rulesProfile, mock(MavenPluginExecutor.class), mavenProject, settings, pomSourceImporter, resourcePerspectives);
  }

  @Test
  public void shouldHaveNoMinimumVersion() throws Exception {
    init();
    resultTransferHandler.setWarningNoMinimumVersion(true);

    sensor.analyse(null, null);

    assertThat(issues).hasSize(1);
    assertThat(issues).is(hasIssueOfRule(NoMinimumMavenVersion.class));
  }

  @Test
  public void shouldHaveNoNoMinimumVersion() throws Exception {
    init();
    resultTransferHandler.setWarningNoMinimumVersion(false);

    sensor.analyse(null, null);

    assertThat(issues).isEmpty();
  }

  @Test
  public void shouldHaveUpdates() throws Exception {
    init();
    resultTransferHandler.setPluginUpdates(new ArrayList<ArtifactUpdate>());

    Dependency mockDependency = mock(Dependency.class);
    when(mockDependency.getLocation(any(String.class))).thenReturn(new InputLocation(1, 1));

    ArtifactUpdate update = mock(ArtifactUpdate.class);
    when(update.toString()).thenReturn("a:b:c:d");
    when(update.getDependency()).thenReturn(mockDependency);

    resultTransferHandler.getPluginUpdates().add(update);

    sensor.analyse(null, null);

    assertThat(issues).hasSize(1);
    assertThat(issues).is(hasIssueOfRule(PluginVersion.class));
  }

  @Test
  public void shouldHaveIncompatibleVersion() throws Exception {
    init();
    DisplayPluginUpdatesBridgeMojo.IncompatibleParentAndProjectMavenVersion incompatibleVersion = mock(
        DisplayPluginUpdatesBridgeMojo.IncompatibleParentAndProjectMavenVersion.class);
    resultTransferHandler.setIncompatibleParentAndProjectMavenVersion(incompatibleVersion);

    sensor.analyse(null, null);

    assertThat(issues).hasSize(1);
    assertThat(issues).is(hasIssueOfRule(IncompatibleMavenVersion.class));
  }

  @Test
  public void shouldHaveSomePluginsMissingTheirVersions() throws Exception {
    init();
    Dependency mockDependency = mock(Dependency.class);
    when(mockDependency.getLocation(any(String.class))).thenReturn(new InputLocation(1, 1));

    List<Dependency> missingVersionPlugins = Lists.newArrayList(mockDependency);
    resultTransferHandler.setMissingVersionPlugins(missingVersionPlugins);

    sensor.analyse(null, null);

    assertThat(issues.size()).isEqualTo(1);
    assertThat(issues).is(hasIssueOfRule(MissingPluginVersion.class));
  }

  private Condition<? super List<Issue>> hasIssueOfRule(final Class<? extends MavenRule> ruleClass) {
    return new Condition<List<Issue>>() {
      @Override
      public boolean matches(final List<Issue> issues) {
        String key = ruleClass.getAnnotation(org.sonar.check.Rule.class).key();
        for (Issue issue : issues) {
          if (key.equals(issue.ruleKey().rule())) {
            return true;
          }
        }
        return false;
      }
    };
  }
}
