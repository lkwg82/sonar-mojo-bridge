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
package de.lgohlke;

import org.dozer.Mapper;
import org.dozer.MappingException;

import java.lang.reflect.Field;

public class MyBeanMapper implements Mapper {

  @Override
  public <T> T map(final Object source, final Class<T> destinationClass) throws MappingException {
    T instance;
    try {
      ClassLoader loader = source.getClass().getClassLoader();

      instance = (T) loader.loadClass(destinationClass.getCanonicalName()).newInstance();
      map(source, instance);
      return instance;
    } catch (InstantiationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public void map(final Object s, final Object t) throws MappingException {

    reflect(s, s.getClass(), t);
  }

  private static void o(final String o) {
    // System.out.println(o);
  }

  private void reflect(final Object s, final Class<?> clazz, final Object t) {

    o("[" + clazz.getCanonicalName() + "]");
    // public static fields
    o("public static");
    for (Field f : clazz.getFields()) {
      _syso(f);
      _value(s, f, t);
    }

    o("private");
    for (Field f : clazz.getDeclaredFields()) {
      _syso(f);
      _value(s, f, t);
      o("");
    }

    if (clazz.getSuperclass() != Object.class) {
      reflect(s, clazz.getSuperclass(), t);
    }
  }

  private void _value(final Object o, final Field f, final Object t) {
    if (f.getModifiers() != 25 && f.getModifiers() != 26) {
      boolean accessible = f.isAccessible();
      if (!accessible) {
        f.setAccessible(true);
      }
      try {
        Object value = f.get(o);
        Object value2 = f.get(t);
        o("\t v1 " + value + " " + f.getModifiers());
        o("\t v2 " + value2);
        f.set(t, value);
        Object value3 = f.get(t);
        o("\t v2 " + value3);
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        if (!accessible) {
          f.setAccessible(accessible);
        }
      }
    }

  }

  private void _syso(final Field f) {
    o("\t " + f.getName() + " type " + f.getType().getCanonicalName());
  }

  @Override
  public <T> T map(final Object source, final Class<T> destinationClass, final String mapId) throws MappingException {
    throw new IllegalStateException("not yet implemented");
  }

  @Override
  public void map(final Object source, final Object destination, final String mapId) throws MappingException {
    throw new IllegalStateException("not yet implemented");
  }

}
