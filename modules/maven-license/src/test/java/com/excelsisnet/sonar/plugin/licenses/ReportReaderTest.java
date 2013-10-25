/*
 * sonar-mojo-bridge-maven-license
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
package com.excelsisnet.sonar.plugin.licenses;

import com.excelsisnet.sonar.plugin.licenses.xml.Dependency;
import org.apache.commons.io.FileUtils;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Fail.fail;

public class ReportReaderTest {
  private final String xml = "" +
      "<dependencies>" +
      "        <dependency>" +
      "                <licenses>" +
      "                        <license>Unknown license</license>" +
      "                </licenses>" +
      "                <groupId>antlr</groupId>" +
      "                <artifactId>antlr</artifactId>" +
      "                <version>2.7.6</version>" +
      "                <url>http://www.antlr.org/</url>" +
      "                <name>AntLR</name>" +
      "        </dependency>" +
      "        <dependency>" +
      "                <licenses>" +
      "                        <license>Public Domain</license>" +
      "                </licenses>" +
      "                <groupId>aopalliance</groupId>" +
      "                <artifactId>aopalliance</artifactId>" +
      "                <version>1.0</version>" +
      "                <url>http://aopalliance.sourceforge.net</url>" +
      "                <name>AOP alliance</name>" +
      "        </dependency>" +
      "</dependencies>";

  @Test
  public void testRead() {

    List<Dependency> dependencies = new ReportReader().read(xml);

    assertThat(dependencies).hasSize(2);
    assertThat(dependencies.get(0).getLicences()).hasSize(1);
    assertThat(dependencies.get(0).getName()).isEqualTo("AntLR");
  }

  @Test
  public void testWriteRead() {

    final Dependency dependency = new Dependency();
    dependency.setGroupId("Antlr");
    dependency.setArtifactId("Antlr");
    dependency.setVersion("2.7.6");
    dependency.setUrl("http://www.antlr.org/");
    dependency.setName("Antlr");
    dependency.setLicences(new ArrayList<String>());
    dependency.getLicences().add("Unknown license");
    dependency.getLicences().add("Unknown license2");
    List<Dependency> dependencies = new ArrayList<Dependency>();
    dependencies.add(dependency);

    ReportReader resultsReader = new ReportReader();
    String xml = resultsReader.write(dependencies);
    resultsReader.read(xml);
  }

  @Test
  public void testFindFile() throws IOException {
    File tempdir = new File(File.createTempFile("adasd", "asdad").getCanonicalPath() + ".d");
    if (tempdir.mkdir()) {
      tempdir.deleteOnExit();

      int limit = new Random().nextInt(100) + 100;
      for (int i = 0; i < limit; i++) {
        new File(tempdir, "" + i).createNewFile();
      }

      int piece = new Random().nextInt(limit);
      final File pieceFile = new File(tempdir, "" + piece);
      FileUtils.write(pieceFile, xml);

      List<Dependency> listOfDependenciesFromReport = new ReportReader().getListOfDependenciesFromReport(tempdir, pieceFile.getName());
      assertThat(listOfDependenciesFromReport).hasSize(2);
    } else {
      fail("could not create tempdir");
    }
  }
}
