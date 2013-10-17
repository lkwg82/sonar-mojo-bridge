/*
 * sonar-mojo-bridge-maven-lint
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
package com.lewisd.maven.lint;

import com.lewisd.maven.lint.rules.AbstractRule;
import de.lgohlke.sonar.maven.MavenRule;
import org.junit.Test;
import org.reflections.Reflections;

import java.lang.reflect.Modifier;
import java.util.Set;

import static org.fest.assertions.api.Assertions.assertThat;

public class RulesTest {

  @Test
  public void testImplementedAllRulesFromModule() {
    Reflections reflections = new Reflections("com.lewisd.maven.lint");
    Set<Class<? extends AbstractRule>> rulesOffered = reflections.getSubTypesOf(AbstractRule.class);
    int countOfAbstractClasses = 0;
    for (Class<? extends AbstractRule> ruleOffered : rulesOffered) {
      if (Modifier.isAbstract(ruleOffered.getModifiers())) {
        countOfAbstractClasses++;
      }
    }

    Set<Class<? extends MavenRule>> rulesImplemented = reflections.getSubTypesOf(MavenRule.class);

    assertThat(rulesImplemented).hasSize(rulesOffered.size() - 1 - countOfAbstractClasses);
  }
}
