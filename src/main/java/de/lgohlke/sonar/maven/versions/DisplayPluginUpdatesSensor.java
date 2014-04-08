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

import de.lgohlke.sonar.PomSourceImporter;
import de.lgohlke.sonar.maven.MavenBaseSensorNG;
import de.lgohlke.sonar.maven.MavenPluginHandlerFactory;
import de.lgohlke.sonar.maven.RuleUtils;
import de.lgohlke.sonar.maven.Rules;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.versions.report.ArtifactUpdate;
import org.codehaus.mojo.versions.report.Dependency;
import org.codehaus.mojo.versions.report.DisplayPluginUpdatesReport;
import org.codehaus.mojo.versions.report.IncompatibleParentAndProjectMavenVersion;
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
import de.lgohlke.sonar.maven.versions.rules.IncompatibleMavenVersion;
import de.lgohlke.sonar.maven.versions.rules.MissingPluginVersion;
import de.lgohlke.sonar.maven.versions.rules.NoMinimumMavenVersion;
import de.lgohlke.sonar.maven.versions.rules.PluginVersion;

import java.util.Map;

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
@Slf4j
public class DisplayPluginUpdatesSensor extends MavenBaseSensorNG {
    private final static String XML_REPORT = "target/versions_plugin_updates_report.xml";
    private final static String GOAL = "display-plugin-updates";

    static final String SENSOR_KEY = de.lgohlke.sonar.Configuration.PLUGIN_KEY + ".pluginUpdates";
    static final String BASE_NAME = "PluginUpdates |";
    static final String WHITELIST_KEY = DisplayPluginUpdatesSensor.SENSOR_KEY + ".whitelist";
    static final String BLACKLIST_KEY = DisplayPluginUpdatesSensor.SENSOR_KEY + ".blacklist";

    private final MavenProject mavenProject;
    private final Settings settings;
    private final PomSourceImporter pomSourceImporter;

    public DisplayPluginUpdatesSensor(RulesProfile rulesProfile,
                                      MavenProject mavenProject,
                                      Settings settings,
                                      ResourcePerspectives resourcePerspectives,
                                      PomSourceImporter pomSourceImporter
    ) {
        super(DisplayPluginUpdatesSensor.log, mavenProject, rulesProfile, resourcePerspectives, settings);
        this.mavenProject = mavenProject;
        this.settings = settings;
        this.pomSourceImporter = pomSourceImporter;
    }

    @Override
    public MavenPluginHandler getMavenPluginHandler(final Project project) {
        final java.util.Properties mavenProjectProperties = mavenProject.getProperties();
        mavenProjectProperties.setProperty("xmlReport", XML_REPORT);
        return MavenPluginHandlerFactory.createHandler(Configuration.BASE_IDENTIFIER + GOAL);
    }

    @Override
    public void analyse(final Project project, final SensorContext context) {
        DisplayPluginUpdatesReport report = getXmlAsFromReport(XML_REPORT, DisplayPluginUpdatesReport.class);

        analyseRuleNoMinimumMavenVersion(report);
        analyseIncompatibleMavenVersion(report);
        analyseMissingVersion(report);
        analysePluginUpdates(report);
    }

    private void analysePluginUpdates(DisplayPluginUpdatesReport report) {
        if (isRuleActive(PluginVersion.class)) {
            Rule rule = RuleUtils.createRuleFrom(PluginVersion.class);
            ArtifactFilter filter = createFilter(settings);
            for (ArtifactUpdate update : report.getPluginUpdates()) {
                if (filter.acceptArtifact(update.toString())) {
                    int line = update.getDependency().getInputLocationMap().get("version").getLine();
                    addIssue(update.toString(), (line > 0) ? line : 1, rule);
                }
            }
        }
    }

    private void analyseMissingVersion(DisplayPluginUpdatesReport report) {
        if (isRuleActive(MissingPluginVersion.class)) {
            String sourceOfPom = pomSourceImporter.getSourceOfPom();
            Rule missingVersionRule = RuleUtils.createRuleFrom(MissingPluginVersion.class);
            for (Dependency dependency : report.getMissingVersionPlugins()) {
                int line = PomUtils.getLine(sourceOfPom, dependency, PomUtils.TYPE.PLUGIN);

                String artifact = dependency.getGroupId() + ":" + dependency.getArtifactId();
                String message = artifact + " has no version";
                addIssue(message, (line > 0) ? line : 1, missingVersionRule);
            }
        }
    }

    private void analyseIncompatibleMavenVersion(DisplayPluginUpdatesReport report) {
        if (isRuleActive(IncompatibleMavenVersion.class)) {
            IncompatibleParentAndProjectMavenVersion incompatibleParentAndProjectMavenVersion = report.getIncompatibleParentAndProjectMavenVersion();
            if (incompatibleParentAndProjectMavenVersion != null) {

                String parentVersion = incompatibleParentAndProjectMavenVersion.getParentVersion();
                String projectVersion = incompatibleParentAndProjectMavenVersion.getProjectVersion();
                String message = "Project does define incompatible minimum versions:  in parent pom " + parentVersion +
                        " and in project pom " + projectVersion;
                Rule rule = RuleUtils.createRuleFrom(IncompatibleMavenVersion.class);
                addIssue(message, 1, rule);
            }
        }
    }

    private void analyseRuleNoMinimumMavenVersion(DisplayPluginUpdatesReport report) {
        if (isRuleActive(NoMinimumMavenVersion.class) && report.isWarnNoMinimumVersion()) {
            Rule rule = RuleUtils.createRuleFrom(NoMinimumMavenVersion.class);
            String message = "Project does not define minimum Maven version, default is: 2.0";
            addIssue(message, 1, rule);
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
