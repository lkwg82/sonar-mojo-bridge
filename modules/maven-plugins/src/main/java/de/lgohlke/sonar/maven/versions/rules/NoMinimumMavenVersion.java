/*
 * sonar-mojo-bridge-maven-plugins
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
package de.lgohlke.sonar.maven.versions.rules;

import de.lgohlke.sonar.maven.MavenRule;
import org.sonar.check.Priority;
import org.sonar.check.Rule;

/**
 * User: lars
 */
@Rule(
        description = NoMinimumMavenVersion.DESCRIPTION,
        key = NoMinimumMavenVersion.KEY,
        name = NoMinimumMavenVersion.NAME,
        priority = Priority.MAJOR)
public interface NoMinimumMavenVersion extends MavenRule {
    String KEY = "NO MINIMUM MAVEN VERSION";
    String NAME = "[POM] found no minimum maven version";
    String DESCRIPTION = "Update the pom.xml to contain <br>" +
            "&lt;prerequisites&gt;<br>" +
            "&nbsp;&nbsp;&lt;maven&gt;&lt;!-- minimum version of Maven that the plugin works with --&gt;&lt;/maven&gt;<br>" +
            "&lt;/prerequisites&gt;";
}
