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
import com.google.common.collect.Lists;
import com.thoughtworks.xstream.XStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class ReportReader {

  public String write(List<Dependency> dependencies) {
    return getxStream().toXML(dependencies);
  }

  public List<Dependency> read(String xml) {
    return (List) getxStream().fromXML(xml);
  }

  private XStream getxStream() {
    XStream xstream = new XStream();
    xstream.setClassLoader(getClass().getClassLoader());
    xstream.autodetectAnnotations(true);
    xstream.alias("dependencies", List.class);
    xstream.alias("dependency", Dependency.class);
    xstream.alias("license", String.class);
    return xstream;
  }

  public List<Dependency> getListOfDependenciesFromReport(File dir, final String filename) {
    try {
      Set<File> files = listFilesRecursive(dir, filename);

      assert files.size() < 2 : "only one file is allowed, but were " + files.size();

      return new ReportReader().read(FileUtils.readFileToString(Lists.newArrayList(files).get(0)));
    } catch (IOException e) {
      log.error(e.getMessage(), e);
      throw new IllegalStateException(e);
    }
  }

  private Set<File> listFilesRecursive(File dir, String filename) {
    HashSet<File> files = new HashSet<File>();

    File[] fileList = dir.listFiles();
    for (File file : fileList) {
      if (file.isDirectory()) {
        files.addAll(listFilesRecursive(file, filename));
      } else {
        if (file.getName().equals(filename)) {
          files.add(file);
        }
      }
    }

    return files;
  }
}
