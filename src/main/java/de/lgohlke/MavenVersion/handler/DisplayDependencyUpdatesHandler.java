/*
 * Sonar maven checks plugin
 * Copyright (C) 2011 ${owner}
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
package de.lgohlke.MavenVersion.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DisplayDependencyUpdatesHandler extends UpdateHandler {
  private static final Logger log = LoggerFactory.getLogger(DisplayDependencyUpdatesHandler.class);
  private static final String KEY_DEPENDENCIES = "The following dependencies in Dependencies have newer versions:";
  private static final String KEY_DEPENDENCY_MGMT = "The following dependencies in Dependency Management have newer versions:";
  private boolean show = false;
  private boolean longLine = false;

  private ArtifactUpdate update;

  @Override
  protected void handleInfoLine(final String line) {

    if (show) {
      show = line.length() > 0;

      if (show) {
        processLine(line);
      }
    }

    if (!show) {
      show = line.startsWith(KEY_DEPENDENCIES) || line.startsWith(KEY_DEPENDENCY_MGMT);
    }
  }

  private void processLine(final String line) {
    String versionRegex = "(\\d[^\\ ]*)";
    String groupidRegex = "([^:]*):";
    final String artifactRegex = "([^\\ ]*)[\\ .]*";
    final String trimmedLine = line.replaceFirst("[\\ ]*", "");

    final String versionsRegex = versionRegex + " -> " + versionRegex;
    final String groupdidArtifactidRegex = groupidRegex + artifactRegex;
    final String onelineRegex = groupdidArtifactidRegex + versionsRegex;

    if (longLine) {
      Pattern pattern = Pattern.compile(versionsRegex);
      Matcher matcher = pattern.matcher(trimmedLine);
      if (matcher.find()) {
        update.setOldVersion(matcher.group(1));
        update.setNewVersion(matcher.group(2));
        getUpdates().add(update);
        longLine = false;
      } else {
        log.error(getClass() + " error matching [second] line: " + line);
      }
    } else {
      if (trimmedLine.matches(onelineRegex)) {

        Pattern pattern = Pattern.compile(onelineRegex);
        Matcher matcher = pattern.matcher(trimmedLine);
        matcher.find();
        update = new ArtifactUpdate();
        update.setGroupId(matcher.group(1));
        update.setArtifactId(matcher.group(2));
        update.setOldVersion(matcher.group(3));
        update.setNewVersion(matcher.group(4));
        getUpdates().add(update);
      } else if (trimmedLine.matches(groupdidArtifactidRegex)) {
        Pattern pattern = Pattern.compile(groupdidArtifactidRegex);
        Matcher matcher = pattern.matcher(trimmedLine);
        matcher.find();
        update = new ArtifactUpdate();
        update.setGroupId(matcher.group(1));
        update.setArtifactId(matcher.group(2));
        longLine = true;
      } else {
        log.error(getClass() + " error matching  line: " + line);
      }
    }
  }
}
