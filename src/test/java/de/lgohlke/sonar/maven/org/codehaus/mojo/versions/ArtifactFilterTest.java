package de.lgohlke.sonar.maven.org.codehaus.mojo.versions;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * User: lars
 */
public class ArtifactFilterTest {

  @Test
  public void testWhitelist() throws Exception {
    ArtifactFilter filter = new ArtifactFilter(".*", "");
    String identifier = "org.apache.karaf.features:spring:3.0.0.RC1";

    assertThat(filter.acceptArtifact(identifier)).isTrue();
  }
  @Test
  public void testBlacklist() throws Exception {
    ArtifactFilter filter = new ArtifactFilter(".*", ".*RC.*");
    String identifier = "org.apache.karaf.features:spring:3.0.0.RC1";

    assertThat(filter.acceptArtifact(identifier)).isFalse();
  }

  @Test
  public void testBlacklistingGroup() throws Exception {
    ArtifactFilter filter = new ArtifactFilter(".*", "[^:]+\\.features:.*");

    assertThat(filter.acceptArtifact("org.apache.karaf.features:spring:3.0.0.RC1")).isFalse();
    assertThat(filter.acceptArtifact("org.apache.karaf.api:spring:3.0.0.RC1")).isTrue();
  }
}
