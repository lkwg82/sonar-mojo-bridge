/*
 * sonar-mojo-bridge-utils
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
import org.sonar.api.BatchComponent;
import org.sonar.api.batch.AbstractSourceImporter;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.SonarIndex;
import org.sonar.api.batch.SupportedEnvironment;
import org.sonar.api.resources.Java;
import org.sonar.api.resources.ProjectFileSystem;

import java.io.File;
import java.util.List;


@SupportedEnvironment("maven")
public class PomSourceImporter extends AbstractSourceImporter implements BatchComponent {
  private final MavenProject project;
  private final SonarIndex index;

  public PomSourceImporter(final MavenProject project, final SonarIndex index) {
    super(Java.INSTANCE);
    this.project = project;
    this.index = index;
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

  public String getSourceOfPom() {
    final org.sonar.api.resources.File file = new org.sonar.api.resources.File("", project.getFile().getName());
    return index.getSource(file);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

}
