/**
 *
 */
package de.lgohlke.sonar.maven.plugin.versions;

import de.lgohlke.sonar.maven.extension.MojoLookupStrategy;
import org.apache.maven.lifecycle.LifecycleExecutor;
import org.apache.maven.lifecycle.internal.MojoExecutor;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.LegacySupport;
import org.apache.maven.plugin.MavenPluginManager;
import org.sonar.batch.MavenPluginExecutor;
import static org.fest.reflect.core.Reflection.field;


/**
 * @author Lars Gohlke
 *
 */
public class VersionMojoLookupStratey implements MojoLookupStrategy {
  private final MavenPluginExecutor mavenPluginExecutor;

  /**
   * @param mavenPluginExecutor
   */
  public VersionMojoLookupStratey(final MavenPluginExecutor mavenPluginExecutor) {
    this.mavenPluginExecutor = mavenPluginExecutor;
  }

  @Override
  public LegacySupport lookupLegacySupport() {
    LifecycleExecutor lifecycleExecutor = field("lifecycleExecutor").ofType(LifecycleExecutor.class)
      .in(mavenPluginExecutor)
      .get();
    MojoExecutor mojoExecutor = field("mojoExecutor").ofType(MojoExecutor.class).in(lifecycleExecutor).get();
    BuildPluginManager pluginManager = field("pluginManager").ofType(BuildPluginManager.class).in(mojoExecutor).get();
    return field("legacySupport").ofType(LegacySupport.class).in(pluginManager).get();
  }

  @Override
  public MavenPluginManager lookupMavenPluginManager() {
    LifecycleExecutor lifecycleExecutor = field("lifecycleExecutor").ofType(LifecycleExecutor.class)
      .in(mavenPluginExecutor)
      .get();
    MojoExecutor mojoExecutor = field("mojoExecutor").ofType(MojoExecutor.class).in(lifecycleExecutor).get();

    return field("mavenPluginManager").ofType(MavenPluginManager.class).in(mojoExecutor).get();
  }
}
