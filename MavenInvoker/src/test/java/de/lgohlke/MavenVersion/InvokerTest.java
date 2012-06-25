package de.lgohlke.MavenVersion;
import com.google.code.tempusfugit.concurrency.ConcurrentTestRunner;
import de.lgohlke.MavenVersion.handler.ArtifactUpdate;
import de.lgohlke.MavenVersion.handler.GOAL;
import de.lgohlke.MavenVersion.handler.UpdateHandler;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.List;

@RunWith(ConcurrentTestRunner.class)
public class InvokerTest {
  static {
    File mvnBinary = new File(System.getenv("_"));
    if (mvnBinary != null) {
      if (mvnBinary.exists())
      {
        System.setProperty("maven.home", mvnBinary.getParentFile().getParent());
      }
    }
  }

  @Test
  public void testGoal_DISPLAY_DEPENDENCY_UPDATES() throws Exception {
    UpdateHandler handler = executeRequest(GOAL.DISPLAY_DEPENDENCY_UPDATES, "pom.xml");

    Assert.assertEquals(3, handler.getUpdates().size());
  }

  @Test
  public void testGoal_DISPLAY_DEPENDENCY_UPDATES_tooLongLines() throws Exception {
    UpdateHandler handler = executeRequest(GOAL.DISPLAY_DEPENDENCY_UPDATES, "toolongline.xml");

    boolean found = false;
    for (ArtifactUpdate update : handler.getUpdates()) {
      if (!found && update.getArtifactId().equals("plexus-container-default")) {
        Assert.assertTrue(update.getOldVersion().equals("1.0-alpha-9-stable-1"));
        found = true;
      }
    }

    Assert.assertTrue(found);
  }

  @Test
  public void testGoal_DISPLAY_DEPENDENCY_UPDATES_withDependencyMgmt() throws Exception {
    UpdateHandler handler = executeRequest(GOAL.DISPLAY_DEPENDENCY_UPDATES, "pom-sonar-squid.xml");

    Assert.assertTrue(handler.getUpdates().size() > 0);
  }

  @Test
  public void testGoal_DISPLAY_PLUGIN_UPDATES_MissingMavenVersion() throws Exception {
    UpdateHandler handler = executeRequest(GOAL.DISPLAY_PLUGIN_UPDATES, "pom_missing_maven_version.xml");
    Assert.assertEquals(0, handler.getUpdates().size());
  }

  @Test
  public void testGoal_DISPLAY_PLUGIN_UPDATES() throws Exception {
    UpdateHandler handler = executeRequest(GOAL.DISPLAY_PLUGIN_UPDATES, "pom.xml");
    final List<ArtifactUpdate> updates = handler.getUpdates();
    Assert.assertEquals(1, updates.size());
    Assert.assertEquals("maven-surefire-plugin", updates.get(0).getArtifactId());
  }

  private static UpdateHandler executeRequest(final GOAL goal, final String pomFilename) throws Exception {

    final UpdateHandler handler = goal.handler().newInstance();
    final File pom = new File("src/test/resources/" + pomFilename);

    new MavenInvoker(pom, handler).run(goal);

    return handler;
  }

}
