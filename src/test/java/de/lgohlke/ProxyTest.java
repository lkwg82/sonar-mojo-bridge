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

import org.testng.annotations.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ProxyTest {

  interface I {

    void test();
  }
  public static class X implements I {
    private final String name;

    public X(final String name) {
      this.name = name;
    }

    @Override
    public void test() {
      test(this.toString());
    }

    private void test(final String name) {
      System.out.println(name);
    }
  }

  @Test
  public void testProxy() {
    X x = new X("1");

    I y = getProxy(I.class, x);
    y.test();
  }

  @SuppressWarnings("unchecked")
  public static <T> T getProxy(final Class<T> intf,
      final T obj) {
    return (T) Proxy.newProxyInstance(obj.getClass().getClassLoader(), new Class<?>[] {intf}, new MyProxy<T>(obj));
  }

  public static class MyProxy<T> implements InvocationHandler {
    final T underlying;

    public MyProxy(final T underlying) {
      this.underlying = underlying;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {

      System.out.println("intercepting " + method);
      return method.invoke(underlying, args);
    }
  }
}
