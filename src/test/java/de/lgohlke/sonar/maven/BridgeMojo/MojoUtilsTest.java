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
package de.lgohlke.sonar.maven.BridgeMojo;

import de.lgohlke.sonar.maven.BridgeMojo.MojoUtils;

import org.apache.maven.plugin.MojoExecutionException;
import org.testng.annotations.Test;

import static org.fest.assertions.Assertions.assertThat;

public class MojoUtilsTest {

  public static class A {
    @SuppressWarnings("unused")
    private static String helloWorld() {
      return "hello world";
    }

    public static void helloWorldPublic() {

    }
  }

  @Test
  public void invokePrivateMethodNoSuchMethod() {
    try {
      MojoUtils.invokePrivateMethod(A.class, "hello");
    } catch (MojoExecutionException e) {
      assertThat(e.getCause()).isExactlyInstanceOf(NoSuchMethodException.class);
    }
  }

  @Test
  public void invokePrivateMethod() throws MojoExecutionException {
    Object result = MojoUtils.invokePrivateMethod(A.class, "helloWorld");
    assertThat((String) result).isEqualTo("hello world");
  }

  @Test
  public void invokePublicMethod() throws MojoExecutionException {
    MojoUtils.invokePrivateMethod(A.class, "helloWorldPublic");
  }
}
