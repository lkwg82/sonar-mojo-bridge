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

public class DisplayPluginUpdatesHandler extends UpdateHandler {
  private final Logger log = LoggerFactory.getLogger(getClass());
  private static final String KEY = "The following plugin updates are available:";
  private boolean show = false;

  private void processLine(final String line) {
    Pattern pattern = Pattern.compile("([^\\ ]*)[\\ .]*((\\d\\.?)+) -> ((\\d\\.?)+)");
    Matcher matcher = pattern.matcher(line.replaceFirst("[\\ ]*", ""));
    if (matcher.find()) {
      ArtifactUpdate update = new ArtifactUpdate();
      update.setArtifactId(matcher.group(1));
      update.setOldVersion(matcher.group(2));
      update.setNewVersion(matcher.group(4));
      getUpdates().add(update);
    } else {
      log.error(getClass() + " error matching line: " + line);
    }
  }

  @Override
  protected void handleInfoLine(final String line) {
    if (show) {
      show = line.length() > 0;

      if (show) {
        processLine(line);
      }
    }

    if (!show) {
      show = line.contains(KEY);
    }
  }
}
