package de.lgohlke.sonar.maven.org.codehaus.mojo.versions;

import lombok.RequiredArgsConstructor;

/**
 * User: lars
 */
@RequiredArgsConstructor
public class ArtifactFilter {
  private final String whitelistRegex;
  private final String blacklistRegex;

  public boolean acceptArtifact(String groupIdArtifactIdVersion) {
    return groupIdArtifactIdVersion.matches(whitelistRegex) && !groupIdArtifactIdVersion.matches(blacklistRegex);
  }
}
