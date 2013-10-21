/*
 * Sonar mojo bridge plugin
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

import de.lgohlke.sonar.maven.lint.LintSensor;
import de.lgohlke.sonar.maven.enforcer.EnforceSensor;
import de.lgohlke.sonar.maven.versions.DisplayDependencyUpdatesSensor;
import de.lgohlke.sonar.maven.versions.DisplayPluginUpdatesSensor;
import de.lgohlke.sonar.maven.versions.UpdateParentPomSensor;
import org.sonar.api.*;
import org.sonar.plugins.xml.language.Xml;

import java.util.Arrays;
import java.util.List;

@Properties(
    {
        @Property(
            key = Configuration.ANALYSIS_ENABLED,
            name = "enable maven analysis",
            description = "Enable maven analysis.",
            defaultValue = Configuration.DEFAULT,
            global = true,
            project = true,
            module = true,
            type = PropertyType.BOOLEAN,
            category = "Mojo Bridge"
        )
    }
)
public class MavenPlugin extends SonarPlugin {

  @Override
  @SuppressWarnings({"rawtypes", "unchecked"})
  public List<Class<? extends Extension>> getExtensions() {
    return Arrays.asList(
        DisplayPluginUpdatesSensor.class,
        DisplayDependencyUpdatesSensor.class,
        UpdateParentPomSensor.class,

        EnforceSensor.class,

        LintSensor.class,
        RulesRepository.class,

        // xml language from xml-plugin
        Xml.class,

        // source importer
        PomSourceImporter.class

        // code colorizer
        // XmlCodeColorizerFormat.class
    );
  }
}
