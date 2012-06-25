package de.lgohlke.MavenVersion.sonar;

import de.lgohlke.MavenVersion.handler.ArtifactUpdate;

public interface MavenRule {
  String getName();

  String getKey();

  String formatMessage(final ArtifactUpdate update);
}
