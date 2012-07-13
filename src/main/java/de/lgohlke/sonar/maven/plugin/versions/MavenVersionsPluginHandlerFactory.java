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
package de.lgohlke.sonar.maven.plugin.versions;

import com.google.common.base.Preconditions;
import de.lgohlke.sonar.maven.Goal;
import de.lgohlke.sonar.maven.MavenPluginHandlerFactory;
import org.sonar.api.batch.maven.MavenPluginHandler;

import java.lang.reflect.Field;
public class MavenVersionsPluginHandlerFactory {
  private static final String baseIdentifier = "org.codehaus.mojo:versions-maven-plugin:1.3.1:";


  public static MavenPluginHandler create(final MavenVersionsGoal help) {

    Field enumField = getEnumField(help);
    Preconditions.checkArgument(enumField.isAnnotationPresent(Goal.class),
        "need to have annotation for the goal");

    String goal = enumField.getAnnotation(Goal.class).value();
    return MavenPluginHandlerFactory.createHandler(baseIdentifier + goal);
  }

  private static Field getEnumField(final MavenVersionsGoal help) {
    try {
      return MavenVersionsGoal.class.getField(help.toString());
    } catch (SecurityException e) {
      e.printStackTrace();
      return null;
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
      return null;
    }
  }
}
