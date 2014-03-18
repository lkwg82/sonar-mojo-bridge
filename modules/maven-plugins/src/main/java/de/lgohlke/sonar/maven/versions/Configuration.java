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
package de.lgohlke.sonar.maven.versions;

import org.sonar.api.Extension;
import de.lgohlke.sonar.maven.versions.rules.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public interface Configuration {
    String BASE_IDENTIFIER = "de.lgohlke.mojo:versions-maven-plugin:2.1.4:";
    String REGEX_DESCRIPTION = "<i>examples:</i><br/>" +
            "exact pattern <tt>org.apache.karaf.features:spring:3.0.0.RC1</tt><br/>" +
            "wildcard <tt>org.apache..*?:spring:.*</tt><br/>" +
            "except RC's pattern <tt>[^:].*?:[^:].*?:[^:].*RC.*</tt><br/>";

    String MULTILINE_CONFIGURATION = "<p>(regex are separated by a newline and will be concatentated with logical OR, e.g. <br/>" +
            "<pre>org.apache.*\norg.codehaus.*</pre>" +
            " will be combined as ((org.apache.*)|(org.codehaus.*)) </p>";

    List<Class> RULES = Arrays.asList(
            (Class) DependencyVersion.class, PluginVersion.class, MissingPluginVersion.class,
            IncompatibleMavenVersion.class, NoMinimumMavenVersion.class, ParentPomVersion.class
    );

    Set<Class<? extends Extension>> EXTENSIONS = new HashSet<Class<? extends Extension>>() {{
        add(DisplayPluginUpdatesSensor.class);
        add(DisplayDependencyUpdatesSensor.class);
        add(DisplayParentPomUpdateSensor.class);
    }};
}
