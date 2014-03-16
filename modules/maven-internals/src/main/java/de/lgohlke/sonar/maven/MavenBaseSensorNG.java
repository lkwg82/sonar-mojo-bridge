/*
 * sonar-mojo-bridge-maven-internals
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
package de.lgohlke.sonar.maven;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.lgohlke.sonar.Configuration;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.maven.DependsUponMavenPlugin;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issue;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.ActiveRuleParam;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleParam;
import org.sonar.plugins.xml.language.Xml;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public abstract class MavenBaseSensorNG implements DependsUponMavenPlugin, Sensor {
    private final Logger logger;
    private final MavenProject mavenProject;
    private final RulesProfile rulesProfile;
    private final ResourcePerspectives resourcePerspectives;
    private final Settings settings;

    protected String getXmlFromReport(String pathToXmlReport) {
        final File projectDirectory = mavenProject.getOriginalModel().getPomFile().getParentFile();
        final File xmlReport = new File(projectDirectory, pathToXmlReport);
        try {
            return FileUtils.readFileToString(xmlReport);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new IllegalStateException(e);
        }
    }

    @Override
    public boolean shouldExecuteOnProject(Project project) {
        String prop = settings.getProperties().get(Configuration.ANALYSIS_ENABLED);
        if (prop == null) {
            prop = Configuration.DEFAULT;
        }

        boolean activatedByConfiguration = Boolean.parseBoolean(prop);
        boolean activatedByRules = checkIfAtLeastOneRuleIsEnabled();

        return activatedByConfiguration && activatedByRules;
    }

    protected boolean checkIfAtLeastOneRuleIsEnabled() {
        List<Rule> associatedRules = getAssociatedRules();
        for (ActiveRule activeRule : rulesProfile.getActiveRules()) {
            if (associatedRules.contains(activeRule.getRule())) {
                return true;
            }
        }
        return false;
    }

    private List<Rule> getAssociatedRules() {
        List<Rule> rules = Lists.newArrayList();
        for (Class<? extends MavenRule> ruleClass : getClass().getAnnotation(Rules.class).values()) {
            rules.add(RuleUtils.createRuleFrom(ruleClass));
        }
        return rules;
    }

    protected void addIssue(String message, int line, Rule rule) {
        org.sonar.api.resources.File file = new org.sonar.api.resources.File("", mavenProject.getFile().getName());
        file.setLanguage(Xml.INSTANCE);

        Issuable issuable = resourcePerspectives.as(Issuable.class, file);
        RuleKey ruleKey = RuleKey.of(rule.getRepositoryKey(), rule.getKey());

        Issue issue = issuable.newIssueBuilder().
                line(line).
                message(message).
                ruleKey(ruleKey).
                build();

        issuable.addIssue(issue);
    }

    protected Map<String, String> createRulePropertiesMapFromQualityProfile(Class<? extends MavenRule> ruleClass) {
        Map<String, String> mappedParams = Maps.newHashMap();
        String ruleKey = RuleUtils.createRuleFrom(ruleClass).getKey();
        ActiveRule activeRuleByConfigKey = rulesProfile.getActiveRuleByConfigKey(Configuration.REPOSITORY_KEY, ruleKey);
        if (null != activeRuleByConfigKey) {
            List<ActiveRuleParam> activeRuleParams = activeRuleByConfigKey.getActiveRuleParams();
            for (ActiveRuleParam activeRuleParam : activeRuleParams) {
                mappedParams.put(activeRuleParam.getKey(), activeRuleParam.getValue());
            }

            // fill with default values for params not set
            final List<RuleParam> params = activeRuleByConfigKey.getRule().getParams();
            for (RuleParam ruleParam : params) {
                if (!mappedParams.containsKey(ruleParam.getKey())) {
                    mappedParams.put(ruleParam.getKey(), ruleParam.getDefaultValue());
                }
            }
        }
        return mappedParams;
    }
}
