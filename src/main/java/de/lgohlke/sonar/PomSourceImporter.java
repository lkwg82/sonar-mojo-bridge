/*
 * Sonar Mojo Bridge
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

import org.apache.commons.io.FileUtils;
import org.apache.maven.project.MavenProject;
import org.sonar.api.BatchComponent;
import org.sonar.api.batch.*;
import org.sonar.api.resources.AbstractLanguage;
import org.sonar.api.resources.File;
import org.sonar.api.resources.Project;

import java.io.IOException;


@SupportedEnvironment("maven")
@Phase(name = Phase.Name.PRE)
public class PomSourceImporter implements BatchComponent, Sensor {
    private final MavenProject project;
    private final SonarIndex index;
    private File pom;

    public PomSourceImporter(final MavenProject project, final SonarIndex index) {
        this.project = project;
        this.index = index;
    }

    public String getSourceOfPom() {
        return index.getSource(pom);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void analyse(Project module, SensorContext context) {

        pom = getPom(module);
        index.index(pom);
        try {
            String source = FileUtils.readFileToString(project.getFile());
            index.setSource(pom, source);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getPom(Project module) {
        File file = File.fromIOFile(project.getFile(), module);
        file.setLanguage(new AbstractLanguage("xml", "XML") {
            @Override
            public String[] getFileSuffixes() {
                return new String[]{"xml"};
            }
        });
        return file;
    }

    @Override
    public boolean shouldExecuteOnProject(Project project) {
        return true;
    }
}
