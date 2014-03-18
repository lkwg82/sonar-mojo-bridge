/*
 * Sonar Mojo Bridge
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
package de.lgohlke.sonar;

import org.fest.assertions.core.Condition;
import org.sonar.api.Extension;
import org.sonar.api.batch.Sensor;
import org.testng.annotations.Test;

import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * User: lgohlke
 */
public class MavenPluginTest {
  @Test
  public void shouldHaveImportantExtensions() throws Exception {
    MavenPlugin plugin = new MavenPlugin();

    List<Class<? extends Extension>> extensions = plugin.getExtensions();
    assertThat(extensions).has(classOfSubType(Sensor.class));
    assertThat(extensions).has(classOfSubType(RulesRepository.class));
  }

  private Condition<List<Class<? extends Extension>>> classOfSubType(final Class<?> clazz) {
    return new Condition<List<Class<? extends Extension>>>() {
      @Override
      public boolean matches(List<Class<? extends Extension>> list) {
        for (Class<?> c : list) {
          if (clazz.isAssignableFrom(c)) {
            return true;
          }
        }
        return false;
      }
    };
  }
}
