/*
 * Sonar maven checks plugin
 * Copyright (C) 2012 ${owner}
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
package de.lgohlke.sonar.maven.handler;

import org.apache.maven.shared.invoker.InvocationOutputHandler;

import java.util.ArrayList;
import java.util.List;

public abstract class UpdateHandler implements InvocationOutputHandler {

  private final List<ArtifactUpdate> updates = new ArrayList<ArtifactUpdate>();
  private boolean hasError = false;
  private final StringBuffer errorMessages = new StringBuffer();

  public List<ArtifactUpdate> getUpdates() {
    return updates;
  }

  @Override
  public String toString() {
    StringBuffer b = new StringBuffer();
    b.append("updates from ");
    b.append(this.getClass().getCanonicalName());
    b.append("\n");
    for (ArtifactUpdate update : updates) {
      b.append("\t");
      b.append(update);
      b.append("\n");
    }
    return b.toString();
  }

  public boolean isHasError() {
    return hasError;
  }

  public void setHasError(final boolean hasError) {
    this.hasError = hasError;
  }

  public StringBuffer getErrorMessages() {
    return errorMessages;
  }

  public void consumeLine(final String line) {

    if (checkOnErrors(line)) {
      if (line.startsWith("[INFO] ")) {
        handleInfoLine(line.replaceFirst("\\[INFO\\] ", ""));
      } else if (line.startsWith("[WARNING] ")) {
        handleWarningLine(line.replaceFirst("\\[WARNING\\] ", ""));
      }

    }
  }

  protected abstract void handleInfoLine(final String line);

  protected void handleWarningLine(final String line) {
    // ok
  }

  private boolean checkOnErrors(final String line) {
    if (line.startsWith("[ERROR]")) {
      getErrorMessages().append(line.replaceFirst("\\[ERROR\\] ", ""));
      getErrorMessages().append("\n");
      hasError = true;
    }
    return !hasError;
  }
}
