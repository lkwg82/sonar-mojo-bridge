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
