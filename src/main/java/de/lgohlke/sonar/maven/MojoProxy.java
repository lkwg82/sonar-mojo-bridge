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

import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class MojoProxy<T extends Mojo> extends DynamicProxy<T> {

  public MojoProxy() {
    super(null);
  }

  public abstract void beforeExecution(T mojo);

  public abstract void afterExecution(T mojo);

  public abstract boolean useAlternativeExecute();

  public void alternativeExecute(final T mojo) throws MojoExecutionException, MojoFailureException {
    // can be overriden
  }

  @Override
  public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {

    ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(getUnderLying().getClass().getClassLoader());
    try {
      return invokeProxyMojo(method, args);
    } finally {
      Thread.currentThread().setContextClassLoader(oldClassLoader);
    }
  }

  private Object invokeProxyMojo(final Method method, final Object[] args) throws MojoExecutionException, MojoFailureException, IllegalAccessException, InvocationTargetException {
    Object result = null;
    if (method.getName().equals("execute") && args == null) {
      beforeExecution(getUnderLying());
      if (useAlternativeExecute()) {
        alternativeExecute(getUnderLying());
      } else {
        result = method.invoke(getUnderLying(), args);
      }
      afterExecution(getUnderLying());
    } else {
      result = method.invoke(getUnderLying(), args);
    }
    return result;
  }

}
