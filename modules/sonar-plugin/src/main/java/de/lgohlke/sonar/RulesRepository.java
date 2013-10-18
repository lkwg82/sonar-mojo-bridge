/*
 * Sonar mojo bridge plugin
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
package de.lgohlke.sonar;

import com.google.common.collect.Lists;
import org.sonar.api.rules.AnnotationRuleParser;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleRepository;

import java.util.List;

public class RulesRepository extends RuleRepository {
    private final AnnotationRuleParser ruleParser;

    public RulesRepository(final AnnotationRuleParser ruleParser) {
        super(Configuration.REPOSITORY_KEY, "java");
        setName(Configuration.REPOSITORY_NAME);
        this.ruleParser = ruleParser;
    }

    @Override
    public List<Rule> createRules() {
        return ruleParser.parse(Configuration.REPOSITORY_KEY, getCheckedClasses());
    }

    @SuppressWarnings("rawtypes")
    private static List<Class> getCheckedClasses() {
        List<Class> rules = Lists.newArrayList();
        rules.addAll(de.lgohlke.sonar.maven.versions.Configuration.MAVEN_VERSION_RULES);
        rules.addAll(de.lgohlke.sonar.maven.enforcer.Configuration.RULE_IMPLEMENTATION_REPOSITORY.keySet());
        rules.addAll(de.lgohlke.sonar.maven.lint.Configuration.RULE_IMPLEMENTATION_REPOSITORY);
        return rules;
    }

}
