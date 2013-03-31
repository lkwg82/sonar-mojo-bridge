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

import com.google.common.base.Joiner;
import org.fest.util.Preconditions;

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

  public static int getLine(String source, ArtifactUpdate artifactUpdate, TYPE type) {
    Preconditions.checkNotNull(source);

    String[] lines = source.split("\\r?\\n");

    String groupd = "<groupId>" + artifactUpdate.getDependency().getGroupId() + "</groupId>";
    String artifact = "<artifactId>" + artifactUpdate.getDependency().getArtifactId() + "</artifactId>";
    String version = "<version>" + artifactUpdate.getDependency().getVersion() + "</version>";

    for (int i = 0; i < lines.length; i++) {
      if (lines[i].contains(version)) {
        if (containsEntry(lines, i, type, groupd, artifact, version)) {
          return i + 1;
        }
      }
    }
    return 0;
  }

  private static boolean containsEntry(String[] lines, int currentPosition, TYPE type, String group, String artifact, String version) {

    int start = findStartOrEndOfBlock(lines, currentPosition, -1, type);
    int end = findStartOrEndOfBlock(lines, currentPosition, +1, type);

    String[] part = new String[end - start + 1];
    for (int i = start; i <= end; i++) {
      part[i - start] = lines[i];
    }

    String fullBlock = Joiner.on("").join(part);

    return fullBlock.contains(version) && fullBlock.contains(artifact) && fullBlock.contains(group);
  }

  private static int findStartOrEndOfBlock(String[] lines, int currentPosition, int step, TYPE type) {
    int i = currentPosition;
    int limitToSearch = 10;
    while (i > 0 && Math.abs(currentPosition - i) <= limitToSearch) {
      if (lines[i].contains(type.getStart()) || lines[i].contains(type.getEnd())) {
        return i;
      } else {
        i += step;
      }
    }
    return -1;
  }
}
