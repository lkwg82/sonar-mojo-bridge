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
package de.lgohlke.sonar.maven;

import com.google.common.collect.Lists;
import de.lgohlke.sonar.MavenPlugin;
import de.lgohlke.sonar.MavenRule;
import de.lgohlke.sonar.maven.internals.Maven3ExecutionProcessTest;
import lombok.Getter;
import lombok.Setter;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.versions.HelpMojo;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.maven.MavenPluginHandler;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.ActiveRule;
import org.sonar.batch.MavenPluginExecutor;
import org.sonar.check.Rule;
import org.sonar.maven3.Maven3PluginExecutor;
import org.sonatype.aether.RepositorySystemSession;
import org.testng.annotations.Test;

import java.util.ArrayList;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MavenBaseSensorTest {

  private final MavenPluginExecutor mavenPluginExecutor = mock(MavenPluginExecutor.class);

  private final MavenProject mavenProject = mock(MavenProject.class);

  private final RulesProfile profile = mock(RulesProfile.class);
  private MyMavenVersionSensor mavenVersionSensor;

  @Rule(key = MyRule.key)
  public static interface MyRule extends MavenRule {
    String key = "X";
  }

  @Goal("help")
  public static class MyBridgeMojo extends HelpMojo implements BridgeMojo<Maven3ExecutionProcessTest.MyResultTransferHandler> {
    @Setter
    private Maven3ExecutionProcessTest.MyResultTransferHandler resultHandler;

    @Override
    public void execute() throws MojoExecutionException {
      resultHandler.setPing(true);
    }
  }

  @Rules(values = MyRule.class)
  @SensorConfiguration(
      bridgeMojo = MyBridgeMojo.class,
      resultTransferHandler = MyMavenVersionSensor.MyResultTransferHandler.class,
      mavenBaseIdentifier = "a:b:c:")
  public static class MyMavenVersionSensor extends MavenBaseSensor<MyMavenVersionSensor.MyResultTransferHandler> {
    @Getter
    @Setter
    private boolean rulesEnabled;

    public static class MyResultTransferHandler implements ResultTransferHandler {
    }

    public MyMavenVersionSensor(final RulesProfile rulesProfile, final MavenPluginExecutor mavenPluginExecutor, final MavenProject mavenProject) {
      super(rulesProfile, mavenPluginExecutor, mavenProject);
    }

    protected boolean checkIfAtLeastOneRuleIsEnabled() {
      return rulesEnabled;
    }

    @Override
    public void analyse(final Project project, final SensorContext context) {
      // ok
    }

    @Override
    public MavenPluginHandler getMavenPluginHandler(final Project project) {
      return null;
    }

    @Override
    public BridgeMojoMapper getMojoMapper() {
      return null;
    }
  }

  class Maven3PluginExecutorMock extends Maven3PluginExecutor {
    @SuppressWarnings("unused")
    private MavenSession mavenSession = null;

    public Maven3PluginExecutorMock() {
      super(null, null);
      try {
        PlexusContainer container = new DefaultPlexusContainer();
        final MavenExecutionRequest request = mock(MavenExecutionRequest.class);
        mavenSession = new MavenSession(container, (RepositorySystemSession) null, request, (MavenExecutionResult) null);
      } catch (PlexusContainerException e) {
        throw new IllegalStateException(e);
      }
    }
  }

  @Test
  public void shouldExecuteOnProjectWithMaven3() {
    Maven3PluginExecutor maven3PluginExecutor = new Maven3PluginExecutorMock();
    mavenVersionSensor = new MyMavenVersionSensor(profile, maven3PluginExecutor, mavenProject);
    mavenVersionSensor.setRulesEnabled(true);

    assertThat(mavenVersionSensor.shouldExecuteOnProject(mock(Project.class))).isTrue();
  }

  @Test
  public void shouldNotExecuteOnProjectWithMaven3NotRunninWithMaven3() {
    MavenPluginExecutor mavenXPluginExecutor = mock(MavenPluginExecutor.class);
    mavenVersionSensor = new MyMavenVersionSensor(profile, mavenXPluginExecutor, mavenProject);

    assertThat(mavenVersionSensor.shouldExecuteOnProject(mock(Project.class))).isFalse();
  }

  @Test
  public void shouldExecuteOnProjectWithMaven3Disabled() {
    Maven3PluginExecutor maven3PluginExecutor = new Maven3PluginExecutorMock();
    mavenVersionSensor = new MyMavenVersionSensor(profile, maven3PluginExecutor, mavenProject);

    final Project projectMock = mock(Project.class);
    when(projectMock.getProperty(MavenPlugin.ANALYSIS_ENABLED)).thenReturn("false");
    assertThat(mavenVersionSensor.shouldExecuteOnProject(projectMock)).isFalse();
  }

  @Test
  public void shouldNotRunBecauseNoRuleActivated() {
    when(profile.getActiveRules()).thenReturn(new ArrayList<ActiveRule>());
    mavenVersionSensor = new MyMavenVersionSensor(profile, new Maven3PluginExecutorMock(), mavenProject);

    final Project projectMock = mock(Project.class);
    assertThat(mavenVersionSensor.shouldExecuteOnProject(projectMock)).isFalse();
  }
}
