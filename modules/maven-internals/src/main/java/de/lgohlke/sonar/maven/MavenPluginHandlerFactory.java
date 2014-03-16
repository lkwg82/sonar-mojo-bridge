/*
 * sonar-mojo-bridge-maven-internals
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
package de.lgohlke.sonar.maven;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.sonar.api.batch.maven.MavenPlugin;
import org.sonar.api.batch.maven.MavenPluginHandler;
import org.sonar.api.resources.Project;

import static com.google.common.base.Preconditions.*;


/**
 * wraps the {@link MavenPluginHandler} implementation, into a generic
 *
 * @author Lars Gohlke
 */
public final class MavenPluginHandlerFactory {
    private MavenPluginHandlerFactory() {
    }

    public static MavenPluginHandler createHandler(final String groupArtifactVersionGoalString) {
        checkNotNull(groupArtifactVersionGoalString);
        checkState(groupArtifactVersionGoalString.length() > 0, "no empty string allowed");

        final String[] parts = groupArtifactVersionGoalString.split(":");

        checkArgument(parts.length == 4,
                "the string must be consist of four parts, seperated by : e.g.: org.codehaus.mojo:versions-maven-plugin:1.3.1:help was '" + groupArtifactVersionGoalString + "'");

        return new InnerMavenPluginHandler(parts[0], parts[1], parts[2], parts[3]);
    }

    @Data
    @RequiredArgsConstructor
    private static class InnerMavenPluginHandler implements MavenPluginHandler {
        private final String groupId;
        private final String artifactId;
        private final String version;
        private final String goal;

        public String[] getGoals() {
            return new String[]{goal};
        }

        @Override
        public void configure(final Project project, final MavenPlugin plugin) {
            // no need for
        }

        @Override
        public boolean isFixedVersion() {
            // so far it makes no sense, to be not fixed to a specific version
            return true;
        }
    }

}
