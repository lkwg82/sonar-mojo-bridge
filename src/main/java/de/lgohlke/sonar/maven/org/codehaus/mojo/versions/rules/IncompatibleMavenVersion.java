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
package de.lgohlke.sonar.maven.org.codehaus.mojo.versions.rules;

import de.lgohlke.sonar.MavenRule;
import org.sonar.check.Priority;
import org.sonar.check.Rule;

/**
 * User: lars
 */
@Rule(
    key = IncompatibleMavenVersion.KEY, priority = Priority.MAJOR,
    name = IncompatibleMavenVersion.NAME, description = IncompatibleMavenVersion.DESCRIPTION
)
public class IncompatibleMavenVersion implements MavenRule {
  public static final String KEY = "INCOMPATIBLE MAVEN VERSION";
  public static final String NAME = "[POM] found incompatible maven version";
  public static final String DESCRIPTION = "the parent pom and the project pom define a pair of incompatible minimum versions";
}
