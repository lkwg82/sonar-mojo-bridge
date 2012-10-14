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

import de.lgohlke.sonar.MavenPlugin;
import de.lgohlke.sonar.maven.internals.MavenPluginExecutorProxyInjection;
import de.lgohlke.sonar.maven.internals.MavenPluginHandlerFactory;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.project.MavenProject;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.maven.DependsUponMavenPlugin;
import org.sonar.api.batch.maven.MavenPluginHandler;
import org.sonar.api.resources.Project;
import org.sonar.batch.MavenPluginExecutor;

/**
 * User: lars
 */
public interface MavenBaseSensorI<T extends ResultTransferHandler> extends DependsUponMavenPlugin,Sensor {
  public BridgeMojoMapper<T> getHandler() ;
}
