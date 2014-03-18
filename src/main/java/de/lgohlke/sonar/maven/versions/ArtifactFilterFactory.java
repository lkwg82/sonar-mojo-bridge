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
package de.lgohlke.sonar.maven.versions;

import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.sonar.api.config.Settings;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * User: lars
 */
@Slf4j
public final class ArtifactFilterFactory {

    private ArtifactFilterFactory() {
    }

    private static final String NEWLINE = "\\r?\\n";

    public static ArtifactFilter createFilterFromSettings(Settings settings, String whitelistKey, String blacklistKey) {
        ArtifactFilter filter = new ArtifactFilter();

        String whiteListRegex = settings.getString(whitelistKey);
        if ((whiteListRegex != null) && (whiteListRegex.length() > 0)) {
            filter.addWhitelistRegex(whiteListRegex);
        }

        String blackListRegex = settings.getString(blacklistKey);
        if ((blackListRegex != null) && (blackListRegex.length() > 0)) {
            filter.addBlacklistRegex(blackListRegex);
        }

        ArtifactFilterFactory.log.debug("created filter from settings: {}", filter);

        return filter;
    }

    public static ArtifactFilter createFilterFromMap(Map<String, String> mappedParams, String whitelistKey, String blacklistKey) {
        Preconditions.checkArgument(mappedParams.containsKey(whitelistKey), "could not find key \"" + whitelistKey + "\"");
        Preconditions.checkArgument(mappedParams.containsKey(blacklistKey), "could not find key \"" + blacklistKey + "\"");

        ArtifactFilter filter = new ArtifactFilter();

        for (String regex : mappedParams.get(whitelistKey).split(NEWLINE)) {
            // filter empty lines
            if (regex.length() > 0) {
                filter.addWhitelistRegex(regex);
            }
        }

        for (String regex : mappedParams.get(blacklistKey).split(NEWLINE)) {
            // filter empty lines
            if (regex.length() > 0) {
                filter.addBlacklistRegex(regex);
            }
        }

        ArtifactFilterFactory.log.debug("created filter from map: {}", filter);

        return filter;
    }

    public static ArtifactFilter createFilterFromMerge(ArtifactFilter... filters) {
        ArtifactFilter mergedFilter = new ArtifactFilter();

        Set<String> whiteListRegexSet = new HashSet<String>();
        Set<String> blackListRegexSet = new HashSet<String>();

        for (ArtifactFilter f : filters) {
            ArtifactFilterFactory.log.debug("use filter for merge : {}", f);

            whiteListRegexSet.addAll(f.getWhitelistRegexList());
            blackListRegexSet.addAll(f.getBlacklistRegexList());
        }

        for (String regex : whiteListRegexSet) {
            mergedFilter.addWhitelistRegex(regex);
        }

        for (String regex : blackListRegexSet) {
            mergedFilter.addBlacklistRegex(regex);
        }

        ArtifactFilterFactory.log.debug("created filter from filters: {}", mergedFilter);

        return mergedFilter;
    }
}
