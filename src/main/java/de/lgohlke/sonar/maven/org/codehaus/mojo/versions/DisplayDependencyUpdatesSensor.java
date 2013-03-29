/*
 * Sonar maven checks plugin
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
package de.lgohlke.sonar.maven.org.codehaus.mojo.versions;

import de.lgohlke.sonar.MavenPlugin;
import de.lgohlke.sonar.maven.MavenBaseSensor;
import de.lgohlke.sonar.maven.ResultTransferHandler;
import de.lgohlke.sonar.maven.Rules;
import de.lgohlke.sonar.maven.SensorConfiguration;
import de.lgohlke.sonar.maven.org.codehaus.mojo.versions.rules.DependencyVersion;
import lombok.Getter;
import lombok.Setter;
import org.apache.maven.project.MavenProject;
import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.PropertyType;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.File;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.Violation;
import org.sonar.batch.scan.maven.MavenPluginExecutor;
import org.sonar.plugins.xml.language.Xml;

import java.util.List;
import java.util.Map;

import static de.lgohlke.sonar.maven.org.codehaus.mojo.versions.Configuration.BASE_IDENTIFIER;

@Properties(
    {
        @Property(
            key = DisplayDependencyUpdatesSensor.WHITELIST_KEY,
            name = DisplayDependencyUpdatesSensor.BASE_NAME + " whitelist regex",
            description = "this regex controls whitelisting <br>" +
                "<i>examples:</i><br/>" +
                "exact pattern <tt>org.apache.karaf.features:spring:3.0.0.RC1</tt><br/>" +
                "wildcard <tt>org.apache..*?:spring:.*</tt><br/>", defaultValue = ".*",
            global = false,
            project = true,
            type = PropertyType.STRING
        ),
        @Property(
            key = DisplayDependencyUpdatesSensor.BLACKLIST_KEY,
            name = DisplayDependencyUpdatesSensor.BASE_NAME + " blacklist regex",
            description = "this regex controls blacklisting" + "<i>examples:</i><br/>" +
                "except RC's pattern <tt>[^:].*?:[^:].*?:[^:].*RC.*</tt><br/>",
            defaultValue = "",
            global = false,
            project = true,
            type = PropertyType.STRING
        )
    }
)
@Rules(values = { DependencyVersion.class })
@SensorConfiguration(
    bridgeMojo = DisplayDependencyUpdatesBridgeMojo.class,
    resultTransferHandler = DisplayDependencyUpdatesSensor.DisplayDependencyUpdatesResultHandler.class, mavenBaseIdentifier = BASE_IDENTIFIER
)
public class DisplayDependencyUpdatesSensor extends MavenBaseSensor<DisplayDependencyUpdatesSensor.DisplayDependencyUpdatesResultHandler> {
  static final String SENSOR_KEY = MavenPlugin.PLUGIN_KEY + ".dependencyUpdates";
  static final String BASE_NAME = "DependencyUpdates |";
  static final String WHITELIST_KEY = DisplayDependencyUpdatesSensor.SENSOR_KEY + ".whitelist";
  static final String BLACKLIST_KEY = DisplayDependencyUpdatesSensor.SENSOR_KEY + ".blacklist";

  private final ArtifactFilter filter;

  @Getter
  @Setter
  public static class DisplayDependencyUpdatesResultHandler implements ResultTransferHandler {
    private Map<String, List<ArtifactUpdate>> updateMap;
  }

  public DisplayDependencyUpdatesSensor(RulesProfile rulesProfile,
                                        MavenPluginExecutor mavenPluginExecutor,
                                        MavenProject mavenProject,
                                        Settings settings) {
    super(rulesProfile, mavenPluginExecutor, mavenProject);

    Map<String, String> mappedParams = createRulePropertiesMap(DependencyVersion.class);
    ArtifactFilter filterFromRules = ArtifactFilterFactory.createFilterFromMap(mappedParams, DependencyVersion.RULE_PROPERTY_WHITELIST, DependencyVersion.RULE_PROPERTY_BLACKLIST);
    ArtifactFilter filterFromSettings = ArtifactFilterFactory.createFilterFromSettings(settings, WHITELIST_KEY, BLACKLIST_KEY);

    filter = ArtifactFilterFactory.createFilterFromMerge(filterFromSettings, filterFromRules);
  }

  @Override
  public void analyse(final Project project, final SensorContext context) {
    DisplayDependencyUpdatesResultHandler resultTransferHandler = getMojoMapper().getResultTransferHandler();

    Rule rule = createRuleFrom(DependencyVersion.class);
    final File file = new File("", getMavenProject().getFile().getName());
    file.setLanguage(Xml.INSTANCE);

    for (Map.Entry<String, List<ArtifactUpdate>> entry : resultTransferHandler.getUpdateMap().entrySet()) {
      String section = entry.getKey();
      List<ArtifactUpdate> updates = entry.getValue();
      for (ArtifactUpdate update : updates) {
        if (filter.acceptArtifact(update.toString())) {
          Violation violation = Violation.create(rule, file);
          violation.setLineId(1);

          String hint = "(found in " + section + ")";
          violation.setMessage(update.toString() + " " + hint);
          context.saveViolation(violation);
        }
      }
    }
  }
}
