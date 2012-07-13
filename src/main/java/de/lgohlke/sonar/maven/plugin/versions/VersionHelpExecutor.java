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

import de.lgohlke.sonar.maven.MojoExecutionHandler;
import de.lgohlke.sonar.maven.plugin.DefaultMavenGoalExecutorImpl;
import de.lgohlke.sonar.plugin.MavenRule;
import org.codehaus.mojo.versions.HelpMojo;

import java.util.Arrays;
import java.util.List;


public class VersionHelpExecutor extends DefaultMavenGoalExecutorImpl {
  private final MojoExecutionHandler<HelpMojo, HelpBridgeMojo> mojoExectionHandler = new MojoExecutionHandler<HelpMojo, HelpBridgeMojo>() {
    @Override
    protected void beforeExecution2(final HelpBridgeMojo mojo) {
      // ok
    }

    @Override
    protected void afterExecution2(final HelpBridgeMojo mojo) {
      // ok
    }

    @Override
    public Class<HelpMojo> getOriginalMojo() {
      return HelpMojo.class;
    }

    @Override
    public Class<HelpBridgeMojo> getReplacingMojo() {
      return HelpBridgeMojo.class;
    }
  };


  @Override
  protected List<? extends MavenRule> getMavenRules() {
    return Arrays.asList(new DependencyVersionMavenRule());
  }

  @Override
  protected MojoExecutionHandler<?, ?> getMojoExectionHandler() {
    return mojoExectionHandler;
  }

}
