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
package de.lgohlke.sonar.maven.org.apache.maven.plugins.enforcer.DependencyConvergence;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Joiner;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.sonar.api.rules.Violation;
import org.testng.annotations.Test;

import java.io.File;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

/**
 * User: lars
 */
public class DependencyConvergenceViolationAdapterTest {
  @Test
  public void test() {
    List<List<DependencyNode>> errors = Lists.newArrayList();
    List<DependencyNode> nodeList = Lists.newArrayList();
    errors.add(nodeList);

    final String groupId = "org.codehaus.plexus";
    final String artifactId = "plexus-classworlds";
    final String classifier = "x";
    final String v1 = "2.4";
    final String v2 = "2.2.2";
    final String v3 = "2.2.3";
    nodeList.add(new DependencyNode(new DefaultArtifact(groupId, artifactId, v1, "compile", "jar", classifier, null)));
    nodeList.add(new DependencyNode(new DefaultArtifact(groupId, artifactId, v2, "compile", "jar", classifier, null)));
    nodeList.add(new DependencyNode(new DefaultArtifact(groupId, artifactId, v3, "provided", "jar", classifier, null)));

    DependencyConvergenceViolationAdapter violationAdapter = getViolationAdapter();
    violationAdapter.setErrors(errors);
    final List<Violation> violations = violationAdapter.getViolations();

    assertThat(violations).hasSize(1);
    assertThat(violations.get(0).getMessage()).startsWith("found multiple version for " + groupId + ":" + artifactId + " (" + Joiner.on(",").join(v1, v2, v3));
  }

  private DependencyConvergenceViolationAdapter getViolationAdapter() {
    MavenProject mavenProject = new MavenProject();
    mavenProject.setFile(new File(""));
    return new DependencyConvergenceViolationAdapter(mavenProject);
  }

}
