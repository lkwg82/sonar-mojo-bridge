/*
 * sonar-mojo-bridge-maven-versions
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
package de.lgohlke.sonar.maven.versions;

import de.lgohlke.sonar.PomSourceImporter;
import de.lgohlke.sonar.maven.MavenBaseSensor;
import de.lgohlke.sonar.maven.RuleUtils;
import de.lgohlke.sonar.maven.Rules;
import de.lgohlke.sonar.maven.SensorConfiguration;
import de.lgohlke.sonar.maven.versions.rules.IncompatibleMavenVersion;
import de.lgohlke.sonar.maven.versions.rules.MissingPluginVersion;
import de.lgohlke.sonar.maven.versions.rules.NoMinimumMavenVersion;
import de.lgohlke.sonar.maven.versions.rules.PluginVersion;
import lombok.Getter;
import lombok.Setter;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.PropertyType;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.Rule;
import org.sonar.batch.scan.maven.MavenPluginExecutor;

import java.util.List;
import java.util.Map;

import static de.lgohlke.sonar.maven.versions.Configuration.BASE_IDENTIFIER;

@Properties(
    {
        @Property(
            key = DisplayPluginUpdatesSensor.WHITELIST_KEY, name = DisplayPluginUpdatesSensor.BASE_NAME + " whitelist regex",
            description = "this regex controls whitelisting",
            defaultValue = ".*",
            global = false,
            project = true,
            type = PropertyType.REGULAR_EXPRESSION,
            category = "Mojo Bridge"
        ),
        @Property(
            key = DisplayPluginUpdatesSensor.BLACKLIST_KEY,
            name = DisplayPluginUpdatesSensor.BASE_NAME + " blacklist regex",
            description = "this regex controls blacklisting",
            defaultValue = "",
            global = false,
            project = true,
            type = PropertyType.REGULAR_EXPRESSION,
            category = "Mojo Bridge"
        )
    }
)
@Rules(
    values = {
        IncompatibleMavenVersion.class,
        MissingPluginVersion.class,
        PluginVersion.class,
        NoMinimumMavenVersion.class
    }
)
@SensorConfiguration(
    bridgeMojo = DisplayPluginUpdatesBridgeMojo.class, resultTransferHandler = DisplayPluginUpdatesSensor.ResultTransferHandler.class,
    mavenBaseIdentifier = BASE_IDENTIFIER
)
public class DisplayPluginUpdatesSensor extends MavenBaseSensor<DisplayPluginUpdatesSensor.ResultTransferHandler> {
  static final String SENSOR_KEY = de.lgohlke.sonar.Configuration.PLUGIN_KEY + ".pluginUpdates";
  static final String BASE_NAME = "PluginUpdates |";
  static final String WHITELIST_KEY = DisplayPluginUpdatesSensor.SENSOR_KEY + ".whitelist";
  static final String BLACKLIST_KEY = DisplayPluginUpdatesSensor.SENSOR_KEY + ".blacklist";

  private final Settings settings;
  private final PomSourceImporter pomSourceImporter;

  @Getter
  @Setter
  public static class ResultTransferHandler implements de.lgohlke.sonar.maven.ResultTransferHandler {
    private List<ArtifactUpdate> pluginUpdates;
    private List<Dependency> missingVersionPlugins;
    private boolean warningNoMinimumVersion;
    private DisplayPluginUpdatesBridgeMojo.IncompatibleParentAndProjectMavenVersion incompatibleParentAndProjectMavenVersion;
  }

  public DisplayPluginUpdatesSensor(RulesProfile rulesProfile,
                                    MavenPluginExecutor mavenPluginExecutor,
                                    MavenProject mavenProject,
                                    Settings settings,
                                    PomSourceImporter pomSourceImporter,
                                    ResourcePerspectives resourcePerspectives) {
    super(rulesProfile, mavenPluginExecutor, mavenProject, resourcePerspectives);
    this.settings = settings;
    this.pomSourceImporter = pomSourceImporter;
  }

  @Override
  public void analyse(final Project project, final SensorContext context) {
    ResultTransferHandler resultTransferHandler = getMojoMapper().getResultTransferHandler();

    // minimum version warning
    if (resultTransferHandler.isWarningNoMinimumVersion()) {
      Rule rule = RuleUtils.createRuleFrom(NoMinimumMavenVersion.class);
      String message = "Project does not define minimum Maven version, default is: 2.0";
      addIssue(message, 1, rule);
    }

    // incompatible minimum versions
    DisplayPluginUpdatesBridgeMojo.IncompatibleParentAndProjectMavenVersion incompatibleParentAndProjectMavenVersion =
        resultTransferHandler.getIncompatibleParentAndProjectMavenVersion();
    if (incompatibleParentAndProjectMavenVersion != null) {

      ArtifactVersion parentVersion = incompatibleParentAndProjectMavenVersion.getParentVersion();
      ArtifactVersion projectVersion = incompatibleParentAndProjectMavenVersion.getProjectVersion();
      String message = "Project does define incompatible minimum versions:  in parent pom " + parentVersion +
          " and in project pom " + projectVersion;
      Rule rule = RuleUtils.createRuleFrom(IncompatibleMavenVersion.class);
      addIssue(message, 1, rule);
    }

    String sourceOfPom = pomSourceImporter.getSourceOfPom();

    // missing versions
    Rule missingVersionRule = RuleUtils.createRuleFrom(MissingPluginVersion.class);
    for (Dependency dependency : resultTransferHandler.getMissingVersionPlugins()) {
      int line = PomUtils.getLine(sourceOfPom, dependency, PomUtils.TYPE.plugin);

      String artifact = dependency.getGroupId() + ":" + dependency.getArtifactId();
      String message = artifact + " has no version";
      addIssue(message, (line > 0) ? line : 1, missingVersionRule);
    }

    // updates
    Rule rule = RuleUtils.createRuleFrom(PluginVersion.class);
    ArtifactFilter filter = createFilter(settings);
    for (ArtifactUpdate update : resultTransferHandler.getPluginUpdates()) {
      if (filter.acceptArtifact(update.toString())) {
        int line = PomUtils.getLine(sourceOfPom, update.getDependency(), PomUtils.TYPE.plugin);
        addIssue(update.toString(), (line > 0) ? line : 1, rule);
      }
    }
  }

  private ArtifactFilter createFilter(Settings settings) {
    Map<String, String> mappedParams = createRulePropertiesMapFromQualityProfile(PluginVersion.class);
    String whitelist = PluginVersion.RULE_PROPERTY_WHITELIST;
    String blacklist = PluginVersion.RULE_PROPERTY_BLACKLIST;
    ArtifactFilter filterFromRules = ArtifactFilterFactory.createFilterFromMap(mappedParams, whitelist, blacklist);
    ArtifactFilter filterFromSettings = ArtifactFilterFactory.createFilterFromSettings(settings, WHITELIST_KEY, BLACKLIST_KEY);

    return ArtifactFilterFactory.createFilterFromMerge(filterFromSettings, filterFromRules);
  }
}
