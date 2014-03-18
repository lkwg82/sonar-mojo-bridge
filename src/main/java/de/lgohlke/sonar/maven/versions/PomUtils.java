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

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement;
import org.codehaus.mojo.versions.report.Dependency;

import java.util.Arrays;

/**
 * User: lars
 */
public final class PomUtils {
    public enum TYPE {
        plugin {
            @Override
            String getStart() {
                return "<plugin>";
            }

            @Override
            String getEnd() {
                return "</plugin>";
            }
        };

        abstract String getStart();

        abstract String getEnd();
    }

    private PomUtils() {
    }

    public static int getLine(String source, Dependency dependency, TYPE type) {
        Preconditions.checkNotNull(source);
        Preconditions.checkNotNull(dependency);

        String[] lines = source.split("\\r?\\n");

        String groupd = "<groupId>" + dependency.getGroupId() + "</groupId>";
        String artifact = "<artifactId>" + dependency.getArtifactId() + "</artifactId>";
        String version = "";

        if ((dependency.getVersion() != null) && (dependency.getVersion().length() > 0)) {
            version = "<version>" + dependency.getVersion() + "</version>";
        }

        String token = (version.length() > 0) ? version : artifact;

        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains(token) && containsEntry(lines, i, type, groupd, artifact, version)) {
                return i + 1;
            }
        }
        return 0;
    }

    @IgnoreJRERequirement
    private static boolean containsEntry(String[] lines, int currentPosition, TYPE type, String group, String artifact, String version) {
        int start = findStartOrEndOfBlock(lines, currentPosition, -1, type);
        int end = findStartOrEndOfBlock(lines, currentPosition, +1, type);

        String[] parts = Arrays.copyOfRange(lines, start, end);
        String fullBlock = Joiner.on("").join(parts);

        return fullBlock.contains(version) && fullBlock.contains(artifact) && fullBlock.contains(group);
    }

    private static int findStartOrEndOfBlock(String[] lines, int currentPosition, int step, TYPE type) {
        int i = currentPosition;
        while ((i > 0) && (Math.abs(currentPosition - i) < lines.length)) {
            if (lines[i].contains(type.getStart()) || lines[i].contains(type.getEnd())) {
                return i;
            } else {
                i += step;
            }
        }
        return -1;
    }
}
