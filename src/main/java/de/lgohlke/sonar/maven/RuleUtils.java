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
package de.lgohlke.sonar.maven;

import de.lgohlke.sonar.Configuration;
import org.sonar.api.rules.Rule;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: lars
 */
public final class RuleUtils {
  private RuleUtils() {
  }

  public static Rule createRuleFrom(Class<? extends MavenRule> ruleClass) {
    final org.sonar.check.Rule annotation = ruleClass.getAnnotation(org.sonar.check.Rule.class);
    checkNotNull(annotation, "each " + MavenRule.class + " needs a " + org.sonar.check.Rule.class + " annotation");

    final String key = annotation.key();
    checkArgument(key.length() > 0, "key should be empty");

    return Rule.create(Configuration.REPOSITORY_KEY, key);
  }
}
