/*
 * Sonar maven checks plugin
 * Copyright (C) 2011 ${owner}
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
package de.lgohlke.MavenVersion;

import com.google.code.tempusfugit.concurrency.ConcurrentTestRunner;
import de.lgohlke.MavenVersion.handler.ArtifactUpdate;
import de.lgohlke.MavenVersion.handler.GOAL;
import de.lgohlke.MavenVersion.handler.UpdateHandler;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@RunWith(ConcurrentTestRunner.class)
public class InvokerTest {
  static {

    Set<String> keySet = System.getenv().keySet();
    List<String> keyList = new ArrayList<String>(keySet);
    Collections.sort(keyList, new Comparator<String>() {

      @Override
      public int compare(final String o1, final String o2) {
        return o1.toLowerCase().compareTo(o2.toLowerCase());
      }
    });

    for (String key : keyList) {
      System.err.println(String.format("%30s %s", key, System.getenv().get(key)));
    }

    final String currentProgramm = System.getenv("_");
    if (currentProgramm == null) {
      throw new IllegalStateException("as of now, we need maven to run the test, could not run without");
    } else {
      File mvnBinary = new File(currentProgramm);
      if (mvnBinary != null) {
        if (mvnBinary.exists())
        {
          System.setProperty("maven.home", mvnBinary.getParentFile().getParent());
        }
      }
    }
  }

  @Test
  public void testGoal_DISPLAY_DEPENDENCY_UPDATES() throws Exception {
    UpdateHandler handler = executeRequest(GOAL.DISPLAY_DEPENDENCY_UPDATES, "pom.xml");

    Assert.assertEquals(2, handler.getUpdates().size());
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
