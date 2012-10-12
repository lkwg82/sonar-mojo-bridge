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

import com.google.common.collect.Maps;
import de.lgohlke.sonar.maven.BridgeMojo;
import de.lgohlke.sonar.maven.Goal;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.mojo.versions.DisplayPluginUpdatesMojo;
import java.util.List;
import java.util.Map;


@Goal(Configuration.Goals.DISPLAY_PLUGIN_UPDATES)
@SuppressWarnings("deprecation")
public class DisplayPluginUpdatesBridgeMojo extends DisplayPluginUpdatesMojo implements BridgeMojo<DisplayUpdatesBridgeMojoResultHandler> {
  private final Map<String, List<ArtifactUpdate>> updateMap = Maps.newHashMap();
  private DisplayUpdatesBridgeMojoResultHandler handler;

  public DisplayPluginUpdatesBridgeMojo() {
    super();
  }

  @Override
  public void injectResultHandler(final DisplayUpdatesBridgeMojoResultHandler handler) {
    this.handler = handler;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void execute() throws MojoExecutionException, MojoFailureException {
    super.execute();
    handler.setUpdateMap(updateMap);
  }
}
