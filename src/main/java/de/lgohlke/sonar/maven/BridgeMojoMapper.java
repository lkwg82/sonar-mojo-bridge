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
package de.lgohlke.sonar.maven;


import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@RequiredArgsConstructor
public abstract class BridgeMojoMapper {

  @Getter
  @NonNull
  private final String goal;

  @Getter
  @NonNull
  private final Class<? extends BridgeMojo<?>> bridgeMojoClass;

  @Getter
  @NonNull
  private final ResultTransferHandler<?> resultTransferHandler;

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
    if (this.goal.equals(goal)) {
      bridgeMojo.injectResultHandler(resultTransferHandler);
    } else {
      throw new BridgeMojoMapperException("no matching " + ResultTransferHandler.class.getSimpleName() + " for goal : " + goal);
    }
  }
}
