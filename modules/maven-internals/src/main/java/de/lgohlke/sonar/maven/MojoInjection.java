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

import lombok.RequiredArgsConstructor;


/**
 * User: lars
 */
@RequiredArgsConstructor
public class MojoInjection {
  private final BridgeMojoMapper bridgeMojoMapper;

  public String getGoal() {
    return bridgeMojoMapper.getGoal();
  }

  public Class<Object> getBridgeMojoClass() {
    return bridgeMojoMapper.getBridgeMojoClass();
  }

  public void setTransferHandler(BridgeMojo<?> result) throws BridgeMojoMapperException {
    bridgeMojoMapper.injectResultTransferHandler(result);
  }

  @Override
  public String toString() {
    return "bridge mojo" + getBridgeMojoClass() + "goal" + getGoal();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof MojoInjection)) {
      return false;
    }

    MojoInjection that = (MojoInjection) o;

    if (!bridgeMojoMapper.equals(that.bridgeMojoMapper)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return bridgeMojoMapper.hashCode();
  }
}
