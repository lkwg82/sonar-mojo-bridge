/*
 * Sonar Mojo Bridge
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

import de.lgohlke.sonar.maven.MavenBaseSensorNG;
import de.lgohlke.sonar.maven.MavenPluginHandlerFactory;
import de.lgohlke.sonar.maven.RuleUtils;
import de.lgohlke.sonar.maven.Rules;
import de.lgohlke.sonar.maven.versions.rules.DependencyVersion;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.versions.report.ArtifactUpdate;
import org.codehaus.mojo.versions.report.DisplayDependencyUpdatesReport;
import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.PropertyType;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.maven.MavenPluginHandler;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.Rule;

import java.util.List;
import java.util.Map;

@Properties(
        {
                @Property(
                        key = DisplayDependencyUpdatesSensor.WHITELIST_KEY, name = DisplayDependencyUpdatesSensor.BASE_NAME + " whitelist regex",
                        description = "this regex controls whitelisting <br>" +
                                "<i>examples:</i><br/>" +
                                "exact pattern <tt>org.apache.karaf.features:spring:3.0.0.RC1</tt><br/>" +
                                "wildcard <tt>org.apache..*?:spring:.*</tt><br/>",
                        defaultValue = ".*",
                        global = false,
                        project = true,
                        type = PropertyType.STRING,
                        category = "Mojo Bridge"
                ),
                @Property(
                        key = DisplayDependencyUpdatesSensor.BLACKLIST_KEY,
                        name = DisplayDependencyUpdatesSensor.BASE_NAME + " blacklist regex",
                        description = "this regex controls blacklisting" + "<i>examples:</i><br/>" +
                                "except RC's pattern <tt>[^:].*?:[^:].*?:[^:].*RC.*</tt><br/>",
                        defaultValue = "",
                        global = false,
                        project = true,
                        type = PropertyType.STRING,
                        category = "Mojo Bridge"
                )
        }
)
@Rules(values = {DependencyVersion.class})
@Slf4j
public class DisplayDependencyUpdatesSensor extends MavenBaseSensorNG {
    private final static String XML_REPORT = "target/versions_dependency_updates_report.xml";
    private final static String GOAL = "display-dependency-updates";

    static final String SENSOR_KEY = de.lgohlke.sonar.Configuration.PLUGIN_KEY + ".dependencyUpdates";
    static final String BASE_NAME = "DependencyUpdates |";
    static final String WHITELIST_KEY = DisplayDependencyUpdatesSensor.SENSOR_KEY + ".whitelist";
    static final String BLACKLIST_KEY = DisplayDependencyUpdatesSensor.SENSOR_KEY + ".blacklist";

    private final MavenProject mavenProject;
    private final Settings settings;

    public DisplayDependencyUpdatesSensor(RulesProfile rulesProfile,
                                          MavenProject mavenProject,
                                          Settings settings,
                                          ResourcePerspectives resourcePerspectives
    ) {
        super(DisplayDependencyUpdatesSensor.log, mavenProject, rulesProfile, resourcePerspectives, settings);
        this.mavenProject = mavenProject;
        this.settings = settings;
    }

    @Override
    public MavenPluginHandler getMavenPluginHandler(final Project project) {
        final java.util.Properties mavenProjectProperties = mavenProject.getProperties();
        mavenProjectProperties.setProperty("xmlReport", XML_REPORT);
        return MavenPluginHandlerFactory.createHandler(Configuration.BASE_IDENTIFIER + GOAL);
    }

    @Override
    public void analyse(final Project project, final SensorContext context) {
        DisplayDependencyUpdatesReport report = getXmlAsFromReport(XML_REPORT, DisplayDependencyUpdatesReport.class);

        Rule rule = RuleUtils.createRuleFrom(DependencyVersion.class);
        ArtifactFilter filter = createFilter(settings);

        for (Map.Entry<String, List<ArtifactUpdate>> entry : report.getUpdatePerSectionMap().entrySet()) {
            List<ArtifactUpdate> updates = entry.getValue();
            for (ArtifactUpdate update : updates) {
                if (filter.acceptArtifact(update.toString()) && verifyVersionIsFromThisProject(project, update)) {
                    int line = update.getDependency().getInputLocationMap().get("version").getLine();
                    addIssue(project,updateToString(update), line, rule);
                }
            }
        }
    }

    private static String updateToString(ArtifactUpdate update) {
        StringBuilder result = new StringBuilder();

        result.append(update.getDependency().getGroupId());
        result.append(":");
        result.append(update.getDependency().getArtifactId());
        result.append(":");
        result.append(update.getDependency().getVersion());

        result.append(" has newer version (");
        result.append(update.getVersionUpdate());
        result.append(") available");

        return result.toString();
    }

    private boolean verifyVersionIsFromThisProject(Project project, ArtifactUpdate update) {
        String currentProjectIdentifier = project.getEffectiveKey() + ":" + project.getAnalysisVersion();
        String modelId = update.getDependency().getInputLocationMap().get("").getInputSource().getModelId();
        return modelId.equals(currentProjectIdentifier);
    }

    private ArtifactFilter createFilter(Settings settings) {
        Map<String, String> mappedParams = createRulePropertiesMapFromQualityProfile(DependencyVersion.class);
        ArtifactFilter filterFromRules = ArtifactFilterFactory.createFilterFromMap(mappedParams, DependencyVersion.RULE_PROPERTY_WHITELIST,
                DependencyVersion.RULE_PROPERTY_BLACKLIST);
        ArtifactFilter filterFromSettings = ArtifactFilterFactory.createFilterFromSettings(settings, WHITELIST_KEY, BLACKLIST_KEY);

        return ArtifactFilterFactory.createFilterFromMerge(filterFromSettings, filterFromRules);
    }
}
