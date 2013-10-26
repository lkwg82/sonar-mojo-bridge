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

import com.google.common.collect.Sets;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.codehaus.plexus.PlexusContainer;
import org.fest.reflect.exception.ReflectionError;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import static org.fest.reflect.core.Reflection.field;

public class PlexusContainerProxy<T extends PlexusContainer> extends DynamicProxy<T> {
  private Set<MojoInjection> injections = Sets.newHashSet();

  public PlexusContainerProxy(final T underlying) {
    super(underlying);
  }

  public void addInjection(MojoInjection injection) {
    if (injections.contains(injection)) {
      injections.remove(injection);
    }
    injections.add(injection);
  }

  @Override
  public Object invoke(final Object proxy, final Method method, final Object[] args) {
    if ("addComponentDescriptor".equals(method.getName())) {
      MojoDescriptor descriptor = (MojoDescriptor) args[0];
      checkGoal(descriptor);
    }

    Object result;
    try {
      result = method.invoke(getUnderLying(), args);
      if ("lookup".equals(method.getName())) {
        checkMojoInstance(result);
      }
    } catch (IllegalAccessException e) {
      throw new ReflectionError(e.getMessage(), e);
    } catch (InvocationTargetException e) {
      throw new ReflectionError(e.getMessage(), e);
    } catch (BridgeMojoMapperException e) {
      throw new ReflectionError(e.getMessage(), e);
    }

    return result;
  }

  @SuppressWarnings("unchecked")
  private void checkMojoInstance(Object result) throws BridgeMojoMapperException {
    for (MojoInjection injection : injections) {
      if (injection.getBridgeMojoClass().isAssignableFrom(result.getClass())) {
        injection.setTransferHandler((BridgeMojo<?>) result);
        // dont iterate further after already matched goal
        return;
      }
    }
  }

  private void checkGoal(MojoDescriptor descriptor) {
    for (MojoInjection injection : injections) {
      if (injection.getGoal().equals(descriptor.getGoal())) {
        Class<?> bridgeMojoClass = injection.getBridgeMojoClass();
        field("implementation").ofType(String.class).in(descriptor).set(bridgeMojoClass.getCanonicalName());
        return; // dont iterate further after already matched goal
      }
    }
  }
}
