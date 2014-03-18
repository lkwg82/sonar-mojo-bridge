/*
 * Sonar mojo bridge plugin
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
package de.lgohlke.sonar.maven.enforcer;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sonar.api.batch.maven.MavenPlugin;
import org.sonar.api.batch.maven.MavenPluginHandler;
import org.sonar.api.resources.Project;

import java.util.Map;

/**
 * this mavenplugin handler enables us to configure dynamically the rules, activated
 */
@RequiredArgsConstructor
public class EnforceMavenPluginHandler implements MavenPluginHandler, ConfigurableEnforceMavenPluginHandler {
  private final MavenPluginHandler mavenPluginHandler;

  /**
   * key,value,key,value
   */
  @VisibleForTesting
  @Getter
  private final Map<String, String> parameters = Maps.newHashMap();

  @Override
  public String getGroupId() {
    return mavenPluginHandler.getGroupId();
  }

  @Override
  public String getArtifactId() {
    return mavenPluginHandler.getArtifactId();
  }

  @Override
  public String getVersion() {
    return mavenPluginHandler.getVersion();
  }

  @Override
  public boolean isFixedVersion() {
    return mavenPluginHandler.isFixedVersion();
  }

  @Override
  public String[] getGoals() {
    return mavenPluginHandler.getGoals();
  }

  @Override
  public void configure(Project project, MavenPlugin plugin) {
    for (Map.Entry<String, String> entry : parameters.entrySet()) {
      plugin.setParameter(entry.getKey(), entry.getValue());
    }
  }

  @Override
  public ConfigurableEnforceMavenPluginHandler setParameter(final String key, final String value) {
    parameters.put(key, value);
    return this;
  }
}
