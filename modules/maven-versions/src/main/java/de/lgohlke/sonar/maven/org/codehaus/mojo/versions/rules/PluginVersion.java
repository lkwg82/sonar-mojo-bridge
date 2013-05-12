/*
 * sonar-maven-checks-maven-versions
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

import de.lgohlke.sonar.maven.MavenRule;
import de.lgohlke.sonar.maven.org.codehaus.mojo.versions.Configuration;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;

@Rule(
    description = PluginVersion.DESCRIPTION + Configuration.MULTILINE_CONFIGURATION + Configuration.REGEX_DESCRIPTION,
    key = PluginVersion.KEY,
    name = PluginVersion.NAME,
    priority = Priority.MINOR
)
public interface PluginVersion extends MavenRule {
  String KEY = "Old Plugin";
  String DESCRIPTION = "found an updated version for plugin";
  String NAME = "[POM] " + DESCRIPTION;

  String RULE_PROPERTY_WHITELIST = "whitelist";
  @RuleProperty(
      key = RULE_PROPERTY_WHITELIST,
      defaultValue = ".*",
      type = "TEXT",
      description = "this regex controls whitelisting")
  String whiteList = null;

  String RULE_PROPERTY_BLACKLIST = "blacklist";
  @RuleProperty(
      key = RULE_PROPERTY_BLACKLIST,
      defaultValue = "",
      type = "TEXT",
      description = "this regex controls blacklisting")
  String blackList = null;
}
