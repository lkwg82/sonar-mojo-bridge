package de.lgohlke.sonar.maven.org.codehaus.mojo.versions;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.model.Dependency;
import org.testng.annotations.Test;

import java.io.File;

import static org.fest.assertions.api.Assertions.assertThat;

public class PomUtilsTest {

  @Test
  public void testFindLineForDependencyUpdate() throws Exception {
    String pom = "src/test/resources/it/pom-old-dependency.xml";
    String source = FileUtils.readFileToString(new File(pom));

    Dependency dependency = new Dependency();
    dependency.setGroupId("org.codehaus.sonar-plugins");
    dependency.setArtifactId("sonar-xml-plugin");
    dependency.setVersion("0.1");

    ArtifactVersion artifactVersion = new DefaultArtifactVersion("1.0");

    ArtifactUpdate artifactUpdate = new ArtifactUpdate(dependency, artifactVersion);

    assertThat(PomUtils.getLine(source, artifactUpdate)).isEqualTo(19);
  }


}
