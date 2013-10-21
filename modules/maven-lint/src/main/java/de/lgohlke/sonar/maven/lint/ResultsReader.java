/*
 * sonar-mojo-bridge-maven-lint
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
package de.lgohlke.sonar.maven.lint;

import com.thoughtworks.xstream.XStream;
import de.lgohlke.sonar.maven.lint.xml.Location;
import de.lgohlke.sonar.maven.lint.xml.Results;
import de.lgohlke.sonar.maven.lint.xml.Violation;

import java.util.ArrayList;

public class ResultsReader {
  public de.lgohlke.sonar.maven.lint.xml.Results read(String xml) {

    XStream xstream = new XStream();
    xstream.setClassLoader(getClass().getClassLoader());
    xstream.autodetectAnnotations(true);
    xstream.alias("results", Results.class);
    xstream.alias("violation", Violation.class);
    xstream.alias("location", Location.class);

    final Results results = (Results) xstream.fromXML(xml);
    if (results.getViolations() == null) {
      results.setViolations(new ArrayList<de.lgohlke.sonar.maven.lint.xml.Violation>());
    }
    return results;
  }
}
