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
package de.lgohlke.sonar.maven.internals;


import de.lgohlke.sonar.maven.BridgeMojoMapper;
import de.lgohlke.sonar.maven.Maven3SonarEmbedder;
import de.lgohlke.sonar.maven.MyBridgeMojo;
import de.lgohlke.sonar.maven.ResultTransferHandler;
import lombok.Getter;
import lombok.Setter;
import org.apache.maven.execution.MavenSession;
import org.sonar.maven3.Maven3PluginExecutor;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.reflect.core.Reflection.field;


public class Maven3ExecutionProcessTest {
  public static final File MAVEN_HOME = new File("/home/lars/development/tools/apache-maven-3.0.4");
  private Maven3SonarEmbedder embedder;

//  @BeforeTest
  protected void init() throws Exception {
    embedder = Maven3SonarEmbedder.configure().usePomFile("pom.xml").goal("versions:help").setAlternativeMavenHome(MAVEN_HOME).build();
  }

  public class MyResultTransferHandler implements ResultTransferHandler {
    @Getter
    @Setter
    private boolean ping;
  }

  @Test
  public void shouldDecorate() throws Exception, ClassNotFoundException {
    init();

    MavenSession mavenSession = field("embedder.mavenSession").ofType(MavenSession.class).in(embedder).get();
    Maven3PluginExecutor mavenPluginExecutor = new Maven3PluginExecutor(null, mavenSession);
    ClassLoader classLoader = this.getClass().getClassLoader();
    BridgeMojoMapper bridgeMojoMapper = new BridgeMojoMapper<MyResultTransferHandler>(MyBridgeMojo.class, new MyResultTransferHandler());

    MyResultTransferHandler handler = (MyResultTransferHandler) bridgeMojoMapper.getResultTransferHandler();
    Maven3ExecutionProcess.decorate(mavenPluginExecutor, classLoader, bridgeMojoMapper);

    embedder.run();

    assertThat(handler.isPing()).isTrue();
  }

  @Test
  public void shouldNotBeDecoratedTwice() throws Exception, ClassNotFoundException {
    init();

    MavenSession mavenSession = field("embedder.mavenSession").ofType(MavenSession.class).in(embedder).get();
    Maven3PluginExecutor mavenPluginExecutor = new Maven3PluginExecutor(null, mavenSession);
    ClassLoader classLoader = this.getClass().getClassLoader();
    BridgeMojoMapper bridgeMojoMapper1 = new BridgeMojoMapper<MyResultTransferHandler>(MyBridgeMojo.class, new MyResultTransferHandler());
    BridgeMojoMapper bridgeMojoMapper2 = new BridgeMojoMapper<MyResultTransferHandler>(MyBridgeMojo.class, new MyResultTransferHandler());

    Maven3ExecutionProcess.decorate(mavenPluginExecutor, classLoader, bridgeMojoMapper1);
    Maven3ExecutionProcess.decorate(mavenPluginExecutor, classLoader, bridgeMojoMapper2);

    embedder.run();

    MyResultTransferHandler resultTransferHandler1 = (MyResultTransferHandler) bridgeMojoMapper1.getResultTransferHandler();
    MyResultTransferHandler resultTransferHandler2 = (MyResultTransferHandler) bridgeMojoMapper2.getResultTransferHandler();
    assertThat(resultTransferHandler1.isPing()).isTrue();
    assertThat(resultTransferHandler2.isPing()).isFalse();
  }
}
