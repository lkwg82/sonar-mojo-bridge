/*
 * sonar-mojo-bridge-maven-internals
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

import de.lgohlke.sonar.Configuration;
import lombok.Getter;
import lombok.Setter;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.lifecycle.DefaultLifecycleExecutor;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.versions.HelpMojo;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.maven.MavenPluginHandler;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.ActiveRule;
import org.sonar.batch.scan.maven.MavenPluginExecutor;
import org.sonar.check.Rule;
import org.sonar.plugins.maven.DefaultMavenPluginExecutor;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MavenBaseSensorTest {
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
      bridgeMojo = MyBridgeMojo.class, resultTransferHandler = MyMavenVersionSensor.MyResultTransferHandler.class, mavenBaseIdentifier = "a:b:c:"
  )
  public static class MyMavenVersionSensor extends MavenBaseSensor<MyMavenVersionSensor.MyResultTransferHandler> {
    @Getter
    @Setter
    private boolean rulesEnabled;

    public static class MyResultTransferHandler implements ResultTransferHandler {
    }

    public MyMavenVersionSensor(final RulesProfile rulesProfile, final MavenPluginExecutor mavenPluginExecutor, final MavenProject mavenProject, ResourcePerspectives resourcePerspectives, Settings settings) {
      super(rulesProfile, mavenPluginExecutor, mavenProject, resourcePerspectives, settings);
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

    @SuppressWarnings("unchecked")
    @Override
    public BridgeMojoMapper getMojoMapper() {
      return null;
    }
  }

  @Rules(values = MyRule.class)
  @SensorConfiguration(
      bridgeMojo = MyBridgeMojo.class, resultTransferHandler = MyMavenVersionSensor.MyResultTransferHandler.class, mavenBaseIdentifier = "a"
  )
  public static class MyBrokenMavenVersionSensor extends MyMavenVersionSensor {
    public MyBrokenMavenVersionSensor(RulesProfile rulesProfile, MavenPluginExecutor mavenPluginExecutor, MavenProject mavenProject, ResourcePerspectives resourcePerspectives, Settings settings) {
      super(rulesProfile, mavenPluginExecutor, mavenProject, resourcePerspectives, settings);
    }
  }

  private static class Maven3LE extends DefaultLifecycleExecutor {
  }

  private static class Maven3PluginExecutorMock extends DefaultMavenPluginExecutor {
    @SuppressWarnings("unused")
    private static MavenSession mavenSession = null;

    static {
      try {
        PlexusContainer container = new DefaultPlexusContainer();
        final MavenExecutionRequest request = mock(MavenExecutionRequest.class);
        mavenSession = new MavenSession(container, null, request, null);
      } catch (PlexusContainerException e) {
        throw new IllegalStateException(e);
      }
    }

    public Maven3PluginExecutorMock() {
      super(new Maven3LE(), mavenSession);

    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void shouldFailOnWrongSensorconfiguration() {
    MavenPluginExecutor maven3PluginExecutor = new Maven3PluginExecutorMock();
    mavenVersionSensor = new MyBrokenMavenVersionSensor(profile, maven3PluginExecutor, mavenProject, mock(ResourcePerspectives.class), mock(Settings.class));
  }

  @Test
  public void shouldExecuteOnProjectWithMaven3() {
    MavenPluginExecutor maven3PluginExecutor = new Maven3PluginExecutorMock();
    mavenVersionSensor = new MyMavenVersionSensor(profile, maven3PluginExecutor, mavenProject, mock(ResourcePerspectives.class), mock(Settings.class));
    mavenVersionSensor.setRulesEnabled(true);

    assertThat(mavenVersionSensor.shouldExecuteOnProject(mock(Project.class))).isTrue();
  }

  @Test
  public void shouldExecuteOnProjectWithMaven3Disabled() {
    MavenPluginExecutor maven3PluginExecutor = new Maven3PluginExecutorMock();
    Settings settings = mock(Settings.class);
    mavenVersionSensor = new MyMavenVersionSensor(profile, maven3PluginExecutor, mavenProject, mock(ResourcePerspectives.class), settings);

    when(settings.getProperties()).thenReturn(new HashMap<String, String>() {{
      put(Configuration.ANALYSIS_ENABLED, "false");
    }});
    assertThat(mavenVersionSensor.shouldExecuteOnProject(mock(Project.class))).isFalse();
  }

  @Test
  public void shouldNotRunBecauseNoRuleActivated() {
    when(profile.getActiveRules()).thenReturn(new ArrayList<ActiveRule>());
    mavenVersionSensor = new MyMavenVersionSensor(profile, new Maven3PluginExecutorMock(), mavenProject, mock(ResourcePerspectives.class), mock(Settings.class));

    Project projectMock = mock(Project.class);
    assertThat(mavenVersionSensor.shouldExecuteOnProject(projectMock)).isFalse();
  }
}
