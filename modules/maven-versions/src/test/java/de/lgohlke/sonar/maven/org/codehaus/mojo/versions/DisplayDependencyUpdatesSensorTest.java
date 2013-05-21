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
package de.lgohlke.sonar.maven.org.codehaus.mojo.versions;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.lgohlke.sonar.PomSourceImporter;
import de.lgohlke.sonar.maven.org.codehaus.mojo.versions.rules.DependencyVersion;
import lombok.Getter;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.InputLocation;
import org.apache.maven.project.MavenProject;
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RulePriority;
import org.sonar.api.rules.Violation;
import org.sonar.batch.DefaultSensorContext;
import org.sonar.batch.scan.maven.MavenPluginExecutor;
import org.testng.annotations.Test;
import java.util.List;
import java.util.Map;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * User: lgohlke
 */
public class DisplayDependencyUpdatesSensorTest {
  @Test
  public void shouldAnalyse() throws Exception {
    DisplayDependencyUpdatesSensor sensor = getDisplayDependencyUpdatesSensor();

    Map<String, List<ArtifactUpdate>> updateMap = Maps.newHashMap();
    ArtifactUpdate artifactUpdate = mock(ArtifactUpdate.class);

    Dependency mockDependency = mock(Dependency.class);
    when(mockDependency.getLocation(any(String.class))).thenReturn(new InputLocation(1, 1));

    String artifactQualifier = "group:artifact:version:goal";
    when(artifactUpdate.getDependency()).thenReturn(mockDependency);

    ArtifactVersion mockArtifactVersion = mock(ArtifactVersion.class);
    when(mockArtifactVersion.toString()).thenReturn(artifactQualifier);
    when(artifactUpdate.getArtifactVersion()).thenReturn(mockArtifactVersion);

    List<ArtifactUpdate> updateList = Lists.newArrayList(artifactUpdate);
    when(updateList.get(0).toString()).thenReturn(artifactQualifier);
    updateMap.put(DisplayDependencyUpdatesBridgeMojo.DEPENDENCIES, updateList);
    sensor.getMojoMapper().getResultTransferHandler().setUpdateMap(updateMap);

    TestSensorContext context = new TestSensorContext();
    sensor.analyse(mock(Project.class), context);

    assertThat(context.getViolations()).hasSize(1);
    assertThat(context.getViolations().get(0).getMessage()).contains(artifactQualifier);
  }

  private DisplayDependencyUpdatesSensor getDisplayDependencyUpdatesSensor() {
    final RulesProfile rulesProfile = RulesProfile.create("mine", "java");
    final Rule rule = Rule.create(de.lgohlke.sonar.Configuration.REPOSITORY_KEY, DependencyVersion.KEY, DependencyVersion.NAME);
    rule.createParameter(DependencyVersion.RULE_PROPERTY_WHITELIST);
    rule.createParameter(DependencyVersion.RULE_PROPERTY_BLACKLIST);

    final ActiveRule activeRule = rulesProfile.activateRule(rule, RulePriority.MAJOR);
    activeRule.setParameter(DependencyVersion.RULE_PROPERTY_WHITELIST, ".*");
    activeRule.setParameter(DependencyVersion.RULE_PROPERTY_BLACKLIST, "");

    MavenProject mavenProject = TestHelper.getMavenProject();
    Settings settings = Settings.createForComponent(DisplayDependencyUpdatesSensor.class);
    PomSourceImporter pomSourceImporter = TestHelper.getPomSourceImporter();

    return new DisplayDependencyUpdatesSensor(rulesProfile, mock(MavenPluginExecutor.class), mavenProject, settings, pomSourceImporter);
  }

  private static class TestSensorContext extends DefaultSensorContext {
    @Getter
    private List<Violation> violations = Lists.newArrayList();

    public TestSensorContext() {
      super(null, null);
    }

    @Override
    public void saveViolation(Violation violation) {
      violations.add(violation);
    }
  }
}
