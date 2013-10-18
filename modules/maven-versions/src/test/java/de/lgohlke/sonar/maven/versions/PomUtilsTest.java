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
package de.lgohlke.sonar.maven.versions;

import org.apache.maven.model.Dependency;
import org.testng.annotations.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class PomUtilsTest {
  @Test
  public void testFindLineForDependencyUpdate() throws Exception {
    String source = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
        "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
        "  <modelVersion>4.0.0</modelVersion>\n" +
        "  <groupId>MavenInvoker</groupId>\n" +
        "  <artifactId>MavenInvoker</artifactId>\n" +
        "  <version>0.0.1-SNAPSHOT</version>\n" +
        "  <dependencies>\n" +
        "    <dependency>\n" +
        "      <groupId>org.apache.maven.plugins</groupId>\n" +
        "      <artifactId>maven-invoker-plugin</artifactId>\n" +
        "      <version>1.6</version>\n" +
        "      <type>maven-plugin</type>\n" +
        "    </dependency>\n" +
        "    <dependency>\n" +
        "      <groupId>junit</groupId>\n" +
        "      <artifactId>junit</artifactId>\n" +
        "      <version>4.8</version>\n" +
        "    </dependency>\n" +
        "    <dependency>\n" +
        "      <groupId>org.apache.maven</groupId>\n" +
        "      <artifactId>maven-embedder</artifactId>\n" +
        "      <version>3.0.3</version>\n" +
        "    </dependency>\n" +
        "    <dependency>\n" +
        "      <groupId>org.codehaus.mojo</groupId>\n" +
        "      <artifactId>versions-maven-plugin</artifactId>\n" +
        "      <version>1.3.1</version>\n" +
        "      <type>maven-plugin</type>\n" +
        "    </dependency>\n" +
        "  </dependencies>\n" +
        "\n" +
        "  <build>\n" +
        "    <plugins>\n" +
        "      <plugin>\n" +
        "        <groupId>org.apache.maven.plugins</groupId>\n" +
        "        <artifactId>maven-surefire-plugin</artifactId>\n" +
        "        <version>2.10</version>\n" +
        "      </plugin>\n" +
        "    </plugins>\n" +
        "  </build>\n" +
        "</project>";

    Dependency dependency = new Dependency();
    dependency.setGroupId("org.apache.maven.plugins");
    dependency.setArtifactId("maven-surefire-plugin");
    dependency.setVersion("2.10");

    assertThat(PomUtils.getLine(source, dependency, PomUtils.TYPE.plugin)).isEqualTo(37);
  }

  @Test
  public void testFindLineForArtifactWithoutAnyVersion() throws Exception {
    String source = "<project>\n" +
        "<build>\n" +
        "  <plugins>\n" +
        "    <plugin>\n" +
        "       <groupId>a</groupId>\n" +
        "       <artifactId>a</artifactId>\n" +
        "    </plugin>\n" +
        "  </plugins>\n" +
        "</build>\n" +
        "</project>";

    Dependency dependency = new Dependency();
    dependency.setGroupId("a");
    dependency.setArtifactId("a");
    dependency.setVersion("");

    assertThat(PomUtils.getLine(source, dependency, PomUtils.TYPE.plugin)).isEqualTo(6);
  }
}
