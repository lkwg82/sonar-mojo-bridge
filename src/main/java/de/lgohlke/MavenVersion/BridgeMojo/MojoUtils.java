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
package de.lgohlke.MavenVersion.BridgeMojo;

import de.lgohlke.MavenVersion.BridgeMojo.MojoUtilsTest.A;
import org.apache.maven.plugin.MojoExecutionException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MojoUtils {

  public static Object invokePrivateMethod(final Class<?> clazz, final String methodname, final Object[] args, final Class<?>[] parameterTypes) throws MojoExecutionException {
    Method method = null;
    try {
      method = clazz.getDeclaredMethod(methodname, parameterTypes);
      method.setAccessible(true);
      return method.invoke(clazz, args);
    } catch (NoSuchMethodException e) {
      throw new MojoExecutionException(e.getMessage(), e);
    } catch (IllegalAccessException e) {
      throw new MojoExecutionException(e.getMessage(), e);
    } catch (InvocationTargetException e) {
      throw new MojoExecutionException(e.getMessage(), e);
    } finally {
      if (method != null) {
        method.setAccessible(false);
      }
    }

  }

  public static Object invokePrivateMethod(final Class<A> clazz, final String methodname) throws MojoExecutionException {
    return invokePrivateMethod(clazz, methodname, new Object[] {}, new Class<?>[] {});
  }
}
