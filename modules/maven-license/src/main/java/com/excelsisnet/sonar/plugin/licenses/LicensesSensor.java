/*
 * Dependency Licenses
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
import com.google.common.base.Joiner;
import de.lgohlke.sonar.maven.MavenPluginHandlerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.project.MavenProject;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.maven.DependsUponMavenPlugin;
import org.sonar.api.batch.maven.MavenPluginHandler;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.Project;
import org.sonar.check.Priority;

import java.io.*;
import java.util.*;

@RequiredArgsConstructor
@Slf4j
public class LicensesSensor implements DependsUponMavenPlugin, Sensor {
  private final static String FILENAME = "sonar-maven-license." + System.currentTimeMillis() + ".xml";
  private final static String TEMPLATE = "/third-party-file.ftl";

  private final MavenProject mavenProject;
  private final Settings settings;

  @Override
  public MavenPluginHandler getMavenPluginHandler(final Project project) {
    Properties mavenProjectProperties = mavenProject.getProperties();
    mavenProjectProperties.setProperty("license.thirdPartyFilename", FILENAME);

    File tempFile;
    try {
      tempFile = File.createTempFile("template", "ftl");
      InputStream inputStream = getClass().getResourceAsStream(TEMPLATE);
      OutputStream outputStream = new FileOutputStream(tempFile);
      IOUtils.copy(inputStream, outputStream);
    } catch (IOException e) {
      log.error(e.getMessage(), e);
      throw new IllegalStateException(e);
    }

    mavenProjectProperties.setProperty("license.fileTemplate", tempFile.getAbsolutePath());
    return MavenPluginHandlerFactory.createHandler(Configuration.BASE_IDENTIFIER);
  }

  @Override
  public boolean shouldExecuteOnProject(Project project) {
    String prop = settings.getProperties().get(de.lgohlke.sonar.Configuration.ANALYSIS_ENABLED);
    if (prop == null) {
      prop = de.lgohlke.sonar.Configuration.DEFAULT;
    }
    return Boolean.parseBoolean(prop);
  }

  private Measure getMeasure(List<LicenseWrapper> licenses) {

    Collections.sort(licenses, new Comparator<LicenseWrapper>() {
      @Override
      public int compare(LicenseWrapper o1, LicenseWrapper o2) {
        int s1 = (o1.getSeverity() == null ? -1 : o1.getSeverity().ordinal());
        int s2 = (o2.getSeverity() == null ? -1 : o2.getSeverity().ordinal());

        if (s1 == s2) {
          return o1.getName().compareTo(o2.getName());
        } else {
          return (s1 < s2 ? 1 : -1);
        }
      }
    });

    Measure result = new Measure();

    result.setMetric(LicenseMetrics.LICENSE);
    result.setData(Joiner.on("|").join(licenses));
    result.setDate(new Date());

    return result;
  }

  public void analyse(Project project, SensorContext context) {
    List<Dependency> dependencies = getListOfDependenciesFromReport();

    /**
     * TODO here is sth. like a semantic gap:
     *
     * where is defined, which license should be allowed/forbidden?
     */

    List<LicenseWrapper> licenses = new LinkedList<LicenseWrapper>();

    for (Dependency dependency : dependencies) {
      for (String license : dependency.getLicences()) {
        String name = dependency.getName().isEmpty() ? dependency.getGroupId() + ":" + dependency.getArtifactId() : dependency.getName();

        Priority serverity = Priority.values()[new Random().nextInt(Priority.values().length)];
        LicenseWrapper wrapper = new LicenseWrapper(name, license, dependency.getUrl(), serverity, "title");
        licenses.add(wrapper);
      }
    }
    context.saveMeasure(getMeasure(licenses));
  }

  private static List<Dependency> getListOfDependenciesFromReport() {
    try {
      final String baseDir = "target/generated-sources/license/";
      return new ReportReader().read(FileUtils.readFileToString(new File(baseDir + FILENAME)));
    } catch (IOException e) {
      log.error(e.getMessage(), e);
      throw new IllegalStateException(e);
    }
  }
}
