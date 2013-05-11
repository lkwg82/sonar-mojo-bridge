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

import com.google.common.collect.Lists;
import de.lgohlke.sonar.MavenPlugin;
import de.lgohlke.sonar.PomSourceImporter;
import de.lgohlke.sonar.maven.MavenRule;
import de.lgohlke.sonar.maven.org.codehaus.mojo.versions.rules.IncompatibleMavenVersion;
import de.lgohlke.sonar.maven.org.codehaus.mojo.versions.rules.MissingPluginVersion;
import de.lgohlke.sonar.maven.org.codehaus.mojo.versions.rules.NoMinimumMavenVersion;
import de.lgohlke.sonar.maven.org.codehaus.mojo.versions.rules.PluginVersion;
import lombok.Getter;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.InputLocation;
import org.apache.maven.project.MavenProject;
import org.fest.assertions.core.Condition;
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.RulePriority;
import org.sonar.api.rules.Violation;
import org.sonar.batch.DefaultSensorContext;
import org.sonar.batch.scan.maven.MavenPluginExecutor;
import org.sonar.check.Rule;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * User: lgohlke
 */
public class DisplayPluginUpdatesSensorTest {
  private final DisplayPluginUpdatesSensor sensor = getPluginUpdatesSensor();

  private DisplayPluginUpdatesSensor.ResultTransferHandler resultTransferHandler;
  private TestSensorContext context;

  public void init() {
    context = new TestSensorContext();

    resultTransferHandler = sensor.getMojoMapper().getResultTransferHandler();
    resultTransferHandler.setMissingVersionPlugins(new ArrayList<Dependency>());
    resultTransferHandler.setIncompatibleParentAndProjectMavenVersion(null);
    resultTransferHandler.setPluginUpdates(new ArrayList<ArtifactUpdate>());
    resultTransferHandler.setWarninNoMinimumVersion(false);
  }

  private DisplayPluginUpdatesSensor getPluginUpdatesSensor() {
    final RulesProfile rulesProfile = RulesProfile.create("mine", "java");
    final org.sonar.api.rules.Rule rule = org.sonar.api.rules.Rule.create(MavenPlugin.REPOSITORY_KEY, PluginVersion.KEY, PluginVersion.NAME);
    rule.createParameter(PluginVersion.RULE_PROPERTY_WHITELIST).setDefaultValue(".*");
    rule.createParameter(PluginVersion.RULE_PROPERTY_BLACKLIST).setDefaultValue("");
    rulesProfile.activateRule(rule, RulePriority.MAJOR);

    MavenProject mavenProject = TestHelper.getMavenProject();
    Settings settings = Settings.createForComponent(DisplayPluginUpdatesSensor.class);
    PomSourceImporter pomSourceImporter = TestHelper.getPomSourceImporter();

    return new DisplayPluginUpdatesSensor(rulesProfile, mock(MavenPluginExecutor.class), mavenProject, settings, pomSourceImporter);
  }

  @Test
  public void shouldHaveNoMinimumVersion() throws Exception {
    init();
    resultTransferHandler.setWarninNoMinimumVersion(true);

    sensor.analyse(mock(Project.class), context);

    assertThat(context.getViolations()).hasSize(1);
    assertThat(context.getViolations()).is(hasViolationOfRule(NoMinimumMavenVersion.class));
  }

  @Test
  public void shouldHaveNoNoMinimumVersion() throws Exception {
    init();
    resultTransferHandler.setWarninNoMinimumVersion(false);

    sensor.analyse(mock(Project.class), context);

    assertThat(context.getViolations()).isEmpty();
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

    sensor.analyse(mock(Project.class), context);

    assertThat(context.getViolations()).hasSize(1);
    assertThat(context.getViolations()).is(hasViolationOfRule(PluginVersion.class));
  }

  @Test
  public void shouldHaveIncompatibleVersion() throws Exception {
    init();
    DisplayPluginUpdatesBridgeMojo.IncompatibleParentAndProjectMavenVersion incompatibleVersion = mock(DisplayPluginUpdatesBridgeMojo.IncompatibleParentAndProjectMavenVersion.class);
    resultTransferHandler.setIncompatibleParentAndProjectMavenVersion(incompatibleVersion);

    sensor.analyse(mock(Project.class), context);

    assertThat(context.getViolations()).hasSize(1);
    assertThat(context.getViolations()).is(hasViolationOfRule(IncompatibleMavenVersion.class));
  }

  @Test
  public void shouldHaveSomePluginsMissingTheirVersions() throws Exception {
    init();
    Dependency mockDependency = mock(Dependency.class);
    when(mockDependency.getLocation(any(String.class))).thenReturn(new InputLocation(1, 1));
    List<Dependency> missingVersionPlugins = Lists.newArrayList(mockDependency);
    resultTransferHandler.setMissingVersionPlugins(missingVersionPlugins);

    sensor.analyse(mock(Project.class), context);

    assertThat(context.getViolations()).hasSize(1);
    assertThat(context.getViolations()).is(hasViolationOfRule(MissingPluginVersion.class));
  }

  private Condition<? super List<Violation>> hasViolationOfRule(final Class<? extends MavenRule> ruleClass) {
    return new Condition<List<Violation>>() {
      @Override
      public boolean matches(final List<Violation> violations) {
        String key = ruleClass.getAnnotation(Rule.class).key();
        for (Violation violation : violations) {
          if (key.equals(violation.getRule().getKey())) {
            return true;
          }
        }
        return false;
      }
    };
  }

  private static class TestSensorContext extends DefaultSensorContext {
    @Getter
    List<Violation> violations = Lists.newArrayList();

    public TestSensorContext() {
      super(null, null);
    }

    @Override
    public void saveViolation(Violation violation) {
      violations.add(violation);
    }
  }
}
