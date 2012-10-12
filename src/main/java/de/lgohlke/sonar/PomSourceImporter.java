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
package de.lgohlke.sonar;

import com.google.common.collect.Lists;
import org.apache.maven.project.MavenProject;
import org.sonar.api.batch.AbstractSourceImporter;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Java;
import org.sonar.api.resources.ProjectFileSystem;
import java.io.File;
import java.util.List;


public class PomSourceImporter extends AbstractSourceImporter {
  private final MavenProject project;

  public PomSourceImporter(final MavenProject project) {
    super(Java.INSTANCE);
    this.project = project;
  }

  @Override
  protected void analyse(final ProjectFileSystem fileSystem, final SensorContext context) {
    List<File> files = Lists.newArrayList();
    List<File> dirs = Lists.newArrayList();

    // adding the pom.xml
    files.add(project.getFile());
    dirs.add(project.getFile().getParentFile());
    parseDirs(context, files, dirs, false, fileSystem.getSourceCharset());
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

}
