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
package de.lgohlke.sonar.maven.plugin.versions;

import de.lgohlke.sonar.maven.plugin.versions.bridgeMojos.DisplayDependencyUpdatesBridgeMojo;
import de.lgohlke.sonar.maven.plugin.versions.bridgeMojos.DisplayDependencyUpdatesBridgeMojoResultHandler;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import de.lgohlke.sonar.maven.Goal;
import de.lgohlke.sonar.maven.plugin.BridgeMojo;
import de.lgohlke.sonar.maven.plugin.ResultTransferHandler;

import java.util.Map;

public class BridgeMojoMapper {
  private final Map<String, ResultTransferHandler<?>> goalToTransferHandlerMap = ImmutableMap.<String, ResultTransferHandler<?>> builder().
      put(Goals.DISPLAY_DEPENDENCY_UPDATES, new DisplayDependencyUpdatesBridgeMojoResultHandler()).
      build();

  private final Map<String, Class<? extends BridgeMojo<?>>> goalToBridgeMojoMap = ImmutableMap.<String, Class<? extends BridgeMojo<?>>> builder().
      put(Goals.DISPLAY_DEPENDENCY_UPDATES, DisplayDependencyUpdatesBridgeMojo.class).
      build();

  /**
   * injects the {@link ResultTransferHandler} into a {@link BridgeMojo}
   * @param bridgeMojo
   */
  public void injectResultTransferHandler(final BridgeMojo<?> bridgeMojo) {
    Preconditions.checkNotNull(bridgeMojo);
    Preconditions.checkArgument(bridgeMojo.getClass().isAnnotationPresent(Goal.class), "each %s needs an annotation %s", BridgeMojo.class, Goal.class);

    String goal = bridgeMojo.getClass().getAnnotation(Goal.class).value();
    if (goalToTransferHandlerMap.containsKey(goal)) {

      bridgeMojo.injectResultHandler(goalToTransferHandlerMap.get(goal));
    }
  }

  public Class<? extends BridgeMojo<?>> getBridgeMojoClassFor(final String goal) {
    return goalToBridgeMojoMap.get(goal);
  }

}
