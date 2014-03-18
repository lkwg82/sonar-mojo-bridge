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
package de.lgohlke.sonar.maven;

import org.sonar.check.Rule;
import org.testng.annotations.Test;
import static org.fest.assertions.api.Assertions.assertThat;


/**
 * User: lars
 */
public class RuleUtilsTest {
  private static class MavenTestRule implements MavenRule {
  }

  @Rule
  private static class MavenTestRule2 implements MavenRule {
  }

  @Rule(key = "k")
  private static class MavenTestRule3 implements MavenRule {
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldFailOnMissingRuleAnnotation() {
    RuleUtils.createRuleFrom(MavenTestRule.class);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void shouldFailOnEmptyKey() {
    RuleUtils.createRuleFrom(MavenTestRule2.class);
  }

  @Test
  public void test() {
    final org.sonar.api.rules.Rule ruleFrom = RuleUtils.createRuleFrom(MavenTestRule3.class);

    assertThat(ruleFrom).isNotNull();
  }
}
