package de.lgohlke.sonar.maven.plugin.versions;

import de.lgohlke.sonar.plugin.MavenPlugin;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.batch.MavenPluginExecutor;
import org.sonar.maven3.Maven3PluginExecutor;
import org.sonatype.aether.RepositorySystemSession;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MavenVersionSensorTest {

  private final MavenPluginExecutor mavenPluginExecutor = mock(MavenPluginExecutor.class);

  private final MavenProject mavenProject = mock(MavenProject.class);

  private final RulesProfile profile = mock(RulesProfile.class);
  private MavenVersionSensor mavenVersionSensor;

  @BeforeTest
  public void createMavenVersionSensor() throws Exception {
    mavenVersionSensor = new MavenVersionSensor(profile, mavenPluginExecutor, mavenProject);
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
    mavenVersionSensor = new MavenVersionSensor(profile, maven3PluginExecutor, mavenProject);

    assertThat(mavenVersionSensor.shouldExecuteOnProject(mock(Project.class))).isTrue();
  }

  @Test
  public void shouldNotExecuteOnProjectWithMaven3() {
    MavenPluginExecutor mavenXPluginExecutor = mock(MavenPluginExecutor.class);
    mavenVersionSensor = new MavenVersionSensor(profile, mavenXPluginExecutor, mavenProject);

    assertThat(mavenVersionSensor.shouldExecuteOnProject(mock(Project.class))).isFalse();
  }

  @Test
  public void shouldExecuteOnProjectWithMaven3Disabled() {
    Maven3PluginExecutor maven3PluginExecutor = new Maven3PluginExecutorMock();
    mavenVersionSensor = new MavenVersionSensor(profile, maven3PluginExecutor, mavenProject);

    final Project projectMock = mock(Project.class);
    when(projectMock.getProperty(MavenPlugin.ANALYSIS_ENABLED)).thenReturn("false");
    assertThat(mavenVersionSensor.shouldExecuteOnProject(projectMock)).isFalse();
  }
}
