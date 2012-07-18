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
package de.lgohlke;

import de.lgohlke.sonar.maven.plugin.versions.DisplayDependencyUpdatesBridgeMojo;

import com.thoughtworks.xstream.XStream;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.factory.DefaultArtifactFactory;
import org.codehaus.mojo.versions.DisplayDependencyUpdatesMojo;
import org.dozer.Mapper;
import org.testng.annotations.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.reflect.core.Reflection.field;

public class ObjectCopyTest {

  @Test
  public void testDozer() {
    // Mapper mapper = new DozerBeanMapper();
    Mapper mapper = new MyBeanMapper();
    DisplayDependencyUpdatesMojo m1 = new DisplayDependencyUpdatesMojo();
    DisplayDependencyUpdatesBridgeMojo m2 = new DisplayDependencyUpdatesBridgeMojo();
    field("artifactFactory").ofType(ArtifactFactory.class).in(m1).set(new DefaultArtifactFactory());
    mapper.map(m1, DisplayDependencyUpdatesBridgeMojo.class);

    final String xm1 = toXml(m1);
    final String xm2 = toXml(m2);
    // System.out.println(xm1);
    // System.out.println(xm2);
    assertThat(xm1).isEqualTo(xm2);

  }

  private String toXml(final Object o) {
    final XStream xStream = new XStream();
    return xStream.toXML(o);
  }
}
