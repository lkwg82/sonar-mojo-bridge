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
import de.lgohlke.sonar.MavenRule;
import de.lgohlke.sonar.maven.org.codehaus.mojo.versions.rules.NoMinimumMavenVersion;
import lombok.Getter;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.fest.assertions.core.Condition;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.Violation;
import org.sonar.batch.DefaultSensorContext;
import org.sonar.batch.MavenPluginExecutor;
import org.sonar.check.Rule;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * User: lgohlke
 */
public class DisplayPluginUpdatesSensorTest {
  private DisplayPluginUpdatesSensor sensor;
  private DisplayPluginUpdatesResultHandler resultTransferHandler;
  private TestSensorContext context;

  @BeforeTest
  public void init() {
    MavenProject mavenProject = mock(MavenProject.class);
    when(mavenProject.getFile()).thenReturn(new File("."));

    context = new TestSensorContext();
    sensor = new DisplayPluginUpdatesSensor(mock(RulesProfile.class), mock(MavenPluginExecutor.class), mavenProject);
    resultTransferHandler = sensor.getHandler().getResultTransferHandler();
    resultTransferHandler.setMissingVersionPlugins(new ArrayList<Dependency>());
    resultTransferHandler.setIncompatibleParentAndProjectMavenVersion(null);
    resultTransferHandler.setPluginUpdates(new ArrayList<ArtifactUpdate>());
    resultTransferHandler.setWarninNoMinimumVersion(false);
  }

  @Test
  public void shouldHaveNoMinimumVersion() throws Exception {

    resultTransferHandler.setWarninNoMinimumVersion(true);

    sensor.analyse(mock(Project.class), context);

    assertThat(context.getViolations()).hasSize(1);
    assertThat(context.getViolations()).is(hasViolationOfRule(NoMinimumMavenVersion.class));
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
