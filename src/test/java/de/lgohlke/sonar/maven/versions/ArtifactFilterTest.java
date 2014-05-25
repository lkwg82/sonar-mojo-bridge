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

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;


/**
 * User: lars
 */
public class ArtifactFilterTest {
    @Test
    public void testWhitelist() throws Exception {
        ArtifactFilter filter = new ArtifactFilter(".*");
        String identifier = "org.apache.karaf.features:spring:3.0.0.RC1";

        assertThat(filter.acceptArtifact(identifier)).isTrue();
    }

    @Test
    public void testBlacklist() throws Exception {
        ArtifactFilter filter = new ArtifactFilter(".*", ".*RC.*");
        String identifier = "org.apache.karaf.features:spring:3.0.0.RC1";

        assertThat(filter.acceptArtifact(identifier)).isFalse();
    }

    @Test
    public void testBlacklist2() throws Exception {
        ArtifactFilter filter = new ArtifactFilter(".*", "[^:].*?:[^:].*?:[^:].*RC.*");

        String identifierPositive = "org.apache.karaf.RC:spring:3.0.0.M1";
        String identifierNegative = "org.apache.karaf.xy:spring:3.0.0.RC1";

        assertThat(filter.acceptArtifact(identifierPositive)).isTrue();
        assertThat(filter.acceptArtifact(identifierNegative)).isFalse();
    }

    @Test
    public void testBlacklistingGroup() throws Exception {
        ArtifactFilter filter = new ArtifactFilter(".*", "[^:]+\\.features:.*");

        assertThat(filter.acceptArtifact("org.apache.karaf.features:spring:3.0.0.RC1")).isFalse();
        assertThat(filter.acceptArtifact("org.apache.karaf.api:spring:3.0.0.RC1")).isTrue();
    }

    @Test
    public void testBlacklistGroup() {
        ArtifactFilter filter = new ArtifactFilter(".*");
        filter.addBlacklistRegex("[^:]+spring[^:]+:.*").addBlacklistRegex("[^:]+:[^:]*spring[^:]*:.*"); // spring as pattern for artifactId

        assertThat(filter.acceptArtifact("org.spring.test:spring:3.0.0.RC1")).isFalse();
        assertThat(filter.acceptArtifact("org.spring.test:test:3.0.0.RC1")).isFalse();
        assertThat(filter.acceptArtifact("org.test:spring:3.0.0.RC1")).isFalse();
        assertThat(filter.acceptArtifact("org.test:test:1-spring")).isTrue();
    }
}
