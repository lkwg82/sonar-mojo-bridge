/**
 *
 */
package de.lgohlke.sonar.maven;

import de.lgohlke.sonar.maven.extension.MojoLookupStrategy;
import hudson.maven.MavenEmbedder;
import org.apache.maven.plugin.LegacySupport;
import org.apache.maven.plugin.MavenPluginManager;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;


/**
 *
 * @author Lars Gohlke
 *
 */
public class SonarMavenEmbeddedLookupStrategy implements MojoLookupStrategy {
  private final MavenEmbedder embedder;

  /**
   * @param embedder
   */
  public SonarMavenEmbeddedLookupStrategy(final MavenEmbedder embedder) {
    this.embedder = embedder;
  }

  @Override
  public MavenPluginManager lookupMavenPluginManager() {
    try {
      return embedder.lookup(MavenPluginManager.class);
    } catch (ComponentLookupException e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public LegacySupport lookupLegacySupport() {
    try {
      return embedder.lookup(LegacySupport.class);
    } catch (ComponentLookupException e) {
      e.printStackTrace();
      return null;
    }
  }
}
