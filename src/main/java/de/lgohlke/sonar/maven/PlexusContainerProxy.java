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


import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.codehaus.plexus.PlexusContainer;

import java.lang.reflect.Method;

import static org.fest.reflect.core.Reflection.field;

public class PlexusContainerProxy<T extends PlexusContainer> extends DynamicProxy<T> {

  private final BridgeMojoMapper bridgeMojoMapper;

  public PlexusContainerProxy(final T underlying, final BridgeMojoMapper bridgeMojoMapper) {
    super(underlying);
    this.bridgeMojoMapper = bridgeMojoMapper;
  }

  @Override
  public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
    if (method.getName().equals("addComponentDescriptor")) {
      MojoDescriptor descriptor = (MojoDescriptor) args[0];
      Class<?> bridgeMojoClass = bridgeMojoMapper.getBridgeMojoClassFor(descriptor.getGoal());
      if (bridgeMojoClass != null) {
        field("implementation").ofType(String.class).in(descriptor).set(bridgeMojoClass.getCanonicalName());
      }
    }
    Object result = method.invoke(getUnderLying(), args);

    if (method.getName().equals("lookup")) {
      if (result instanceof BridgeMojo) {
        bridgeMojoMapper.injectResultTransferHandler((BridgeMojo<?>) result);
      }
    }
    return result;
  }

}
