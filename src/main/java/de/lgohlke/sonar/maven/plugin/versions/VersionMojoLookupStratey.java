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
/**
 *
 */
package de.lgohlke.sonar.maven.plugin.versions;

import de.lgohlke.sonar.maven.extension.MojoLookupStrategy;
import org.apache.maven.lifecycle.LifecycleExecutor;
import org.apache.maven.lifecycle.internal.MojoExecutor;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.LegacySupport;
import org.apache.maven.plugin.MavenPluginManager;
import org.sonar.batch.MavenPluginExecutor;
import static org.fest.reflect.core.Reflection.field;


/**
 * @author Lars Gohlke
 *
 */
public class VersionMojoLookupStratey implements MojoLookupStrategy {
  private final MavenPluginExecutor mavenPluginExecutor;

  /**
   * @param mavenPluginExecutor
   */
  public VersionMojoLookupStratey(final MavenPluginExecutor mavenPluginExecutor) {
    this.mavenPluginExecutor = mavenPluginExecutor;
  }

  @Override
  public LegacySupport lookupLegacySupport() {
    LifecycleExecutor lifecycleExecutor = field("lifecycleExecutor").ofType(LifecycleExecutor.class)
      .in(mavenPluginExecutor)
      .get();
    MojoExecutor mojoExecutor = field("mojoExecutor").ofType(MojoExecutor.class).in(lifecycleExecutor).get();
    BuildPluginManager pluginManager = field("pluginManager").ofType(BuildPluginManager.class).in(mojoExecutor).get();
    return field("legacySupport").ofType(LegacySupport.class).in(pluginManager).get();
  }

  @Override
  public MavenPluginManager lookupMavenPluginManager() {
    LifecycleExecutor lifecycleExecutor = field("lifecycleExecutor").ofType(LifecycleExecutor.class)
      .in(mavenPluginExecutor)
      .get();
    MojoExecutor mojoExecutor = field("mojoExecutor").ofType(MojoExecutor.class).in(lifecycleExecutor).get();

    return field("mavenPluginManager").ofType(MavenPluginManager.class).in(mojoExecutor).get();
  }
}
