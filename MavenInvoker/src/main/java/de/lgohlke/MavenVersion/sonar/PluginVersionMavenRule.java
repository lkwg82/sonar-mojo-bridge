package de.lgohlke.MavenVersion.sonar;

import de.lgohlke.MavenVersion.handler.ArtifactUpdate;
import org.sonar.check.Priority;
import org.sonar.check.Rule;

@Rule(key = PluginVersionMavenRule.KEY, priority = Priority.MINOR, name = PluginVersionMavenRule.NAME, description = PluginVersionMavenRule.DESCRIPTION)
public class PluginVersionMavenRule implements MavenRule {
  protected final static String KEY = "Old Plugin";
  protected final static String NAME = "[POM] found an updated version for plugin";
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
