/*
 * Sonar maven checks plugin
 * Copyright (C) 2012 ${owner}
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
package de.lgohlke.sonar.plugin;

import de.lgohlke.MavenVersion.sonar.MavenVersionSensor;
import de.lgohlke.MavenVersion.sonar.PomSourceImporter;
import de.lgohlke.MavenVersion.sonar.RulesRepository;

import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.PropertyType;
import org.sonar.api.SonarPlugin;
import org.sonar.plugins.xml.language.Xml;
import org.sonar.plugins.xml.language.XmlCodeColorizerFormat;

import java.util.Arrays;
import java.util.List;

@Properties({
  @Property(key = MavenPlugin.ANALYSIS_ENABLED,
    name = "enable maven analysis",
    description = "Enable maven analysis.",
    defaultValue = MavenPlugin.DEFAULT,
    global = true,
    project = true,
    type = PropertyType.BOOLEAN)})
public class MavenPlugin extends SonarPlugin {

  public static final String ANALYSIS_ENABLED = "sonar.maven.analysis";
  public static final String REPOSITORY_KEY = "maven";
  public static final String REPOSITORY_NAME = "Maven";
  public static final String DEFAULT = "true";

  @SuppressWarnings({"rawtypes", "unchecked"})
  public List getExtensions() {

    return Arrays.asList(
        MavenVersionSensor.class,
        RulesRepository.class,
        // xml language from xml-plugin
        Xml.class,
        // source importer
        PomSourceImporter.class,

        // code colorizer
        XmlCodeColorizerFormat.class);
  }

}
