/*
 * Sonar maven checks plugin
 * Copyright (C) 2011 ${owner}
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
package de.lgohlke.MavenVersion;

import de.lgohlke.MavenVersion.handler.GOAL;
import de.lgohlke.MavenVersion.handler.UpdateHandler;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

import java.io.File;
import java.util.Collections;

public class MavenInvoker {

  private final UpdateHandler handler;
  private final File pom;

  public MavenInvoker(final File pom, final UpdateHandler handler) {
    this.pom = pom;
    this.handler = handler;
  }

  public void run(final GOAL goal) throws MavenInvocationException {
    InvocationRequest request = new DefaultInvocationRequest();
    request.setPomFile(pom);
    request.setOutputHandler(handler);
    request.setGoals(Collections.singletonList(goal.goal()));
    request.setUpdateSnapshots(true);

    Invoker invoker = new DefaultInvoker();
    invoker.execute(request);
  }

}
