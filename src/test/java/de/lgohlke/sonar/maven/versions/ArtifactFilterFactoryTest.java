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

import com.google.common.collect.ImmutableMap;
import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.config.PropertyDefinitions;
import org.sonar.api.config.Settings;
import org.testng.annotations.Test;

import java.util.Map;

import static org.fest.assertions.api.Assertions.assertThat;


/**
 * User: lars
 */
public class ArtifactFilterFactoryTest {
    @Properties({@Property(key = "b", defaultValue = "", name = "")})
    class Component {
    }

    @Test
    public void testCreateFilterFromSettingsWithDefaultBlacklist() throws Exception {
        PropertyDefinitions propertyDefinitions = new PropertyDefinitions(new Component());
        Settings settings = new Settings(propertyDefinitions);

        settings.appendProperty("w", ".*");

        ArtifactFilter filter = ArtifactFilterFactory.createFilterFromSettings(settings, "w", "b");

        assertThat(filter.getWhitelistRegexList()).hasSize(1);
        assertThat(filter.getWhitelistRegexList()).contains(".*");
        assertThat(filter.getBlacklistRegexList()).isEmpty();
    }

    @Test
    public void testCreateFilterFromSettingsWithBlacklist() throws Exception {
        PropertyDefinitions propertyDefinitions = new PropertyDefinitions(new Component());
        Settings settings = new Settings(propertyDefinitions);

        settings.appendProperty("w", ".*");
        settings.appendProperty("b", "x");

        ArtifactFilter filter = ArtifactFilterFactory.createFilterFromSettings(settings, "w", "b");

        assertThat(filter.getWhitelistRegexList()).hasSize(1);
        assertThat(filter.getWhitelistRegexList()).contains(".*");
        assertThat(filter.getBlacklistRegexList()).hasSize(1);
        assertThat(filter.getBlacklistRegexList()).contains("x");
    }

    @Test
    public void testCreateFilterFromMap() throws Exception {
        Map<String, String> map = ImmutableMap.of("w", ".*\n\nx", "b", "\n");
        ArtifactFilter filter = ArtifactFilterFactory.createFilterFromMap(map, "w", "b");

        assertThat(filter.getWhitelistRegexList()).hasSize(2);
        assertThat(filter.getBlacklistRegexList()).isEmpty();
    }

    @Test
    public void testMergeOfFilters() throws Exception {
        ArtifactFilter filter1 = new ArtifactFilter(".*");
        ArtifactFilter filter2 = new ArtifactFilter(".*", "x");

        ArtifactFilter filterFromMerge = ArtifactFilterFactory.createFilterFromMerge(filter1, filter2);

        assertThat(filterFromMerge.getWhitelistRegexList()).hasSize(1);
        assertThat(filterFromMerge.getBlacklistRegexList()).hasSize(1);
    }
}
