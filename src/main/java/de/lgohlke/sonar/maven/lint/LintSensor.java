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
package de.lgohlke.sonar.maven.lint;

import com.google.common.annotations.VisibleForTesting;
import de.lgohlke.sonar.maven.*;
import de.lgohlke.sonar.maven.lint.rules.*;
import de.lgohlke.sonar.maven.lint.xml.Results;
import de.lgohlke.sonar.maven.lint.xml.Violation;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.project.MavenProject;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.maven.MavenPluginHandler;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.Rule;

import java.util.Properties;

@Rules(values = {
        LintDuplicateDependenciesRule.class,
        LintExecutionIdRule.class,
        LintGroupArtifactVersionMustBeInCorrectOrderIdRule.class,
        LintMissingCIManagementRule.class,
        LintMissingIssueManagementRule.class,
        LintMissingDeveloperInformationRule.class,
        LintRedundantDependencyVersionsRule.class,
        LintRedundantPluginVersionsRule.class,
        LintProfileMustOnlyAddModulesRule.class,
        LintVersionPropertiesMustUseDotVersionRule.class,
        LintVersionPropertiesMustUseProjectVersionRule.class
})
@Slf4j
public class LintSensor extends MavenBaseSensorNG {
    private final static String LINT_FILENAME = "target/sonar-maven-lint." + System.currentTimeMillis() + ".xml";
    private final static Object BASE_PREFIX = "lint";

    private final MavenProject mavenProject;

    public LintSensor(MavenProject mavenProject, RulesProfile rulesProfile, ResourcePerspectives resourcePerspectives, Settings settings) {
        super(log, mavenProject, rulesProfile, resourcePerspectives, settings);
        this.mavenProject = mavenProject;
    }

    @Override
    public void analyse(Project project, SensorContext context) {
        Results results = getXmlAsFromReport(LINT_FILENAME, Results.class);

        if (null != results.getViolations()) {
            for (Violation violation : results.getViolations()) {
                Rule rule = createRuleFromViolation(violation);
                if (rule != null) {
                    addIssue(project,violation.getMessage(), violation.getLocation().getLine()+1, rule);
                }
            }
        }
    }

    @Override
    public MavenPluginHandler getMavenPluginHandler(final Project project) {
        final Properties mavenProjectProperties = mavenProject.getProperties();
        mavenProjectProperties.setProperty("maven-lint.failOnViolation", "false");
        mavenProjectProperties.setProperty("maven-lint.output.file.xml", LINT_FILENAME);
        return MavenPluginHandlerFactory.createHandler(Configuration.BASE_IDENTIFIER);
    }

    @VisibleForTesting
    Rule createRuleFromViolation(Violation violation) {
        final Class<? extends MavenRule>[] rulesOfThisSensor = getClass().getAnnotation(Rules.class).values();
        for (Class<? extends MavenRule> ruleClazz : rulesOfThisSensor) {
            org.sonar.check.Rule rule = ruleClazz.getAnnotation(org.sonar.check.Rule.class);
            if (rule.key().equals(BASE_PREFIX + "." + violation.getRule())) {
                return RuleUtils.createRuleFrom(ruleClazz);
            }
        }

        log.warn("rule for violation " + violation.getRule() + " is not implemented yet");
        return null;
    }
}
