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

import de.lgohlke.sonar.maven.Goal;
import de.lgohlke.sonar.maven.plugin.BridgeMojo;
import de.lgohlke.sonar.maven.plugin.ResultTransferHandler;

import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public abstract class BridgeMojoMapper {

  public abstract Map<String, ResultTransferHandler<?>> getGoalToTransferHandlerMap();

  public abstract Map<String, Class<? extends BridgeMojo<?>>> getGoalToBridgeMojoMap();

  /**
   * injects the {@link ResultTransferHandler} into a {@link BridgeMojo}
   * 
   * @param bridgeMojo
   * @throws BridgeMojoMapperException
   */
  public void injectResultTransferHandler(final BridgeMojo<?> bridgeMojo) throws BridgeMojoMapperException {
    checkNotNull(bridgeMojo);
    checkArgument(bridgeMojo.getClass().isAnnotationPresent(Goal.class), "each %s needs an annotation %s", BridgeMojo.class, Goal.class);

    String goal = bridgeMojo.getClass().getAnnotation(Goal.class).value();
    Map<String, ResultTransferHandler<?>> goalToTransferHandlerMap = getGoalToTransferHandlerMap();
    if (goalToTransferHandlerMap.containsKey(goal)) {
      bridgeMojo.injectResultHandler(goalToTransferHandlerMap.get(goal));
    } else {
      throw new BridgeMojoMapperException("no matching " + ResultTransferHandler.class.getSimpleName() + " for goal : " + goal);
    }
  }

  public Class<? extends BridgeMojo<?>> getBridgeMojoClassFor(final String goal) {
    return getGoalToBridgeMojoMap().get(goal);
  }

}
