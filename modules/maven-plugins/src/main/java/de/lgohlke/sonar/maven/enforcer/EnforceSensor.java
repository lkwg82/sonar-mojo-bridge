/*
 * sonar-mojo-bridge-maven-plugins
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
package de.lgohlke.sonar.maven.enforcer;

import de.lgohlke.sonar.maven.*;
import de.lgohlke.sonar.maven.enforcer.DependencyConvergence.DependencyConvergenceRule;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.project.MavenProject;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.maven.MavenPluginHandler;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.Rule;

import java.util.ArrayList;
import java.util.List;

@Rules(values = {DependencyConvergenceRule.class})
@Slf4j
public class EnforceSensor extends MavenBaseSensorNG {
    private final static String GOAL = "enforce";

    private final List<ViolationAdapter> violationAdapters = new ArrayList<ViolationAdapter>();
    private final EnforceMavenPluginHandler mavenPluginHandler;
    private final MavenProject mavenProject;

    public EnforceSensor(RulesProfile rulesProfile, MavenProject mavenProject, ResourcePerspectives resourcePerspectives, Settings settings) {
        super(log, mavenProject, rulesProfile, resourcePerspectives, settings);
        this.mavenProject = mavenProject;

        final MavenPluginHandler pluginHandler = MavenPluginHandlerFactory.createHandler(Configuration.BASE_IDENTIFIER + GOAL);
        this.mavenPluginHandler = new EnforceMavenPluginHandler(pluginHandler);
        initViolationAdapterPerActiveRule(rulesProfile, mavenProject);
    }

    @Override
    public MavenPluginHandler getMavenPluginHandler(final Project project) {
        final java.util.Properties mavenProjectProperties = mavenProject.getProperties();
        mavenProjectProperties.setProperty("fail", "false");
        return mavenPluginHandler;
    }

    private void initViolationAdapterPerActiveRule(RulesProfile rulesProfile, MavenProject mavenProject) {
        for (Class<? extends MavenRule> ruleClass : getClass().getAnnotation(Rules.class).values()) {
            Rule rule = RuleUtils.createRuleFrom(ruleClass);
            for (ActiveRule activeRule : rulesProfile.getActiveRules()) {
                if (rule.equals(activeRule.getRule())) {

                    ViolationAdapter adapter = Configuration.RULE_ADAPTER_MAP.get(ruleClass);
                    adapter.setRule(rule);
                    adapter.setProjectDir(mavenProject.getOriginalModel().getPomFile().getParentFile());
                    adapter.configure(mavenPluginHandler);

                    violationAdapters.add(adapter);
                }
            }
        }
    }

    @Override
    public void analyse(Project project, SensorContext context) {
        for (ViolationAdapter adapter : violationAdapters) {
            for (Violation violation : adapter.getViolations()) {
                addIssue(violation.getMessage(), violation.getLine(), adapter.getRule());
            }
        }
    }
}
