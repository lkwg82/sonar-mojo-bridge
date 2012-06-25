package de.lgohlke.MavenVersion.sonar;

import de.lgohlke.MavenVersion.handler.ArtifactUpdate;
import org.sonar.check.Priority;
import org.sonar.check.Rule;

@Rule(key = DependencyVersionMavenRule.KEY, priority = Priority.MINOR, name = DependencyVersionMavenRule.NAME, description = DependencyVersionMavenRule.DESCRIPTION)
public class DependencyVersionMavenRule implements MavenRule {
  protected final static String KEY = "Old Dependency";
  protected final static String NAME = "[POM] found an updated version for dependency";
  protected final static String DESCRIPTION = "TODO";

  public String getName() {
    return NAME;
  }

  public String getKey() {
    return KEY;
  }

  public String formatMessage(final ArtifactUpdate update) {
    return "update available for: " + update;
  }
}
