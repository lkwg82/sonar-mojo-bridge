/*
 * Sonar maven checks plugin (maven3 support)
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

import com.google.common.collect.ImmutableMap;
import de.lgohlke.sonar.maven.plugin.BridgeMojo;
import de.lgohlke.sonar.maven.plugin.ResultTransferHandler;
import de.lgohlke.sonar.maven.plugin.versions.BridgeMojoMapper;
import hudson.maven.MavenEmbedderException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.mojo.versions.HelpMojo;
import org.sonar.maven3.Maven3PluginExecutor;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Map;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.reflect.core.Reflection.field;

public class Maven3ExecutionProcessTest {
  private static final String MAVEN_HOME_KEY = "maven.home";
  private static final String M2_HOME_KEY = "M2_HOME";
  // public static final File MAVEN_HOME = new File("/data/home/lgohlke/development/tools/apache-maven-3.0.4");
  public static final File MAVEN_HOME = new File("/home/lars/development/tools/apache-maven-3.0.4");
  final String SUB_GOAL = "help";
  final String GOAL = "versions:" + SUB_GOAL;

  @BeforeClass
  protected void setUp() throws Exception {
    System.setProperty(M2_HOME_KEY, "wrong");
    System.setProperty(MAVEN_HOME_KEY, "wrong");
  }

  class MyResultTransferHandler implements ResultTransferHandler<MyResultTransferHandler> {

    private boolean ping;

    public boolean isPing() {
      return ping;
    }

    public void setPing(final boolean ping) {
      this.ping = ping;
    }
  }

  public static class MyBridgeMojo extends HelpMojo implements BridgeMojo<MyResultTransferHandler> {

    private MyResultTransferHandler handler;

    @Override
    public void execute() throws MojoExecutionException {
      handler.setPing(true);
    }

    @Override
    public void injectResultHandler(final ResultTransferHandler<?> handler) {
      this.handler = (MyResultTransferHandler) handler;
    }
  }
  class MyBridgeMojoMapper extends BridgeMojoMapper
  {
    private final Map<String, ResultTransferHandler<?>> map = ImmutableMap.<String, ResultTransferHandler<?>>
    builder().
        put(SUB_GOAL, new MyResultTransferHandler()).
    build();

    @Override
    public Map<String, ResultTransferHandler<?>> getGoalToTransferHandlerMap() {
      return map;
    }

    @Override
    public Map<String, Class<? extends BridgeMojo<?>>> getGoalToBridgeMojoMap() {
      return ImmutableMap.<String, Class<? extends BridgeMojo<?>>>
      builder().
          put(SUB_GOAL, MyBridgeMojo.class).
      build();
    }
  }

  @Test
  public void shouldDecorate() throws MavenEmbedderException {

    MavenSonarEmbedder embedder = MavenSonarEmbedder.configure().
        usePomFile("pom.xml").
        goal(GOAL).
        setAlternativeMavenHome(MAVEN_HOME).
        build();
    MavenSession mavenSession = field("embedder.mavenSession").ofType(MavenSession.class).in(embedder).get();

    Maven3PluginExecutor mavenPluginExecutor = new Maven3PluginExecutor(null, mavenSession);
    ClassLoader classLoader = this.getClass().getClassLoader();
    BridgeMojoMapper bridgeMojoMapper = new MyBridgeMojoMapper();

    MyResultTransferHandler handler = (MyResultTransferHandler) bridgeMojoMapper.getGoalToTransferHandlerMap().get(SUB_GOAL);

    // Maven3ExecutionProcess.decorate(mavenPluginExecutor, classLoader, bridgeMojoMapper);
    embedder.run();


    assertThat(handler.isPing()).isTrue();
  }
}
