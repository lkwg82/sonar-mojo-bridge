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
package de.lgohlke.sonar.maven.org.codehaus.mojo.versions;

import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.config.Settings;

import java.util.Map;

/**
 * User: lars
 */
public class ArtifactFilterFactory {

  private static final String NEWLINE = "\\r?\\n";

  public static ArtifactFilter createFilterFromSettings(Settings settings, String whitelistKey, String blacklistKey) {

    ArtifactFilter filter = new ArtifactFilter();

    String whiteListRegex = settings.getString(whitelistKey);
    if (!whitelistKey.isEmpty()){
      filter.addWhitelistRegex(whiteListRegex);
    }

    String blackListRegex = settings.getString(blacklistKey);
    if (blackListRegex == null) {
      PropertyDefinition definition = settings.getDefinitions().get(blacklistKey);
      blackListRegex = definition.getDefaultValue();
    }

    if (!blackListRegex.isEmpty()){
      filter.addBlacklistRegex(blackListRegex);
    }

    return filter;
  }

  public static ArtifactFilter createFilterFromMap(Map<String, String> mappedParams, String whitelistKey, String blacklistKey) {
    ArtifactFilter filter = new ArtifactFilter();

    for (String regex : mappedParams.get(whitelistKey).split(NEWLINE)) {
      // filter empty lines
      if (!regex.isEmpty()) {
        filter.addWhitelistRegex(regex);
      }
    }

    for (String regex : mappedParams.get(blacklistKey).split(NEWLINE)) {
      // filter empty lines
      if (!regex.isEmpty()) {
        filter.addBlacklistRegex(regex);
      }
    }
    return filter;
  }
}
