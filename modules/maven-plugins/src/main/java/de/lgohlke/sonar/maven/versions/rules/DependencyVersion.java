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
package de.lgohlke.sonar.maven.versions.rules;

import de.lgohlke.sonar.maven.MavenRule;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import de.lgohlke.sonar.maven.versions.Configuration;

@Rule(
        description = DependencyVersion.DESCRIPTION + Configuration.MULTILINE_CONFIGURATION + Configuration.REGEX_DESCRIPTION,
        key = DependencyVersion.KEY,
        name = DependencyVersion.NAME,
        priority = Priority.MINOR
)
public interface DependencyVersion extends MavenRule {
    String KEY = "Old Dependency";
    String NAME = "[POM] found an newer version for a dependency in use";
    String DESCRIPTION = "this dependency has a newer version available";

    String RULE_PROPERTY_WHITELIST = "whitelist";
    @RuleProperty(key = RULE_PROPERTY_WHITELIST, defaultValue = ".*", type = "TEXT", description = "this regex controls whitelisting")
    String WHITE_LIST = null;

    String RULE_PROPERTY_BLACKLIST = "blacklist";
    @RuleProperty(key = RULE_PROPERTY_BLACKLIST, defaultValue = "[^:].*?:[^:].*?:[^:].*(alpha|Alpha|beta|Beta).*", type = "TEXT", description = "this regex controls blacklisting")
    String BLACK_LIST = null;
}
