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
package de.lgohlke.sonar.maven.plugin;

import de.lgohlke.sonar.maven.MavenSonarEmbedder;
import de.lgohlke.sonar.plugin.MavenPlugin;
import de.lgohlke.sonar.plugin.MavenRule;
import hudson.maven.MavenEmbedderException;
import org.apache.maven.project.MavenProject;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.File;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.Violation;
import org.sonar.api.utils.SonarException;

import java.util.List;

public abstract class DefaultMavenGoalExecutorImpl implements MavenGoalExecutor {

  private RulesProfile rulesProfile;
  private MavenProject project;
  private SensorContext context;

  @Override
  public boolean needsToBeExecuted() {
    for (MavenRule rule : getMavenRules()) {
      if (isCurrentRuleActive(rule)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void setRulesProfile(final RulesProfile profile) {
    this.rulesProfile = profile;
  }

  protected boolean isCurrentRuleActive(final MavenRule rule) {
    for (ActiveRule activeRule : rulesProfile.getActiveRules()) {
      if (activeRule.getConfigKey().equals(rule.getKey()) && activeRule.getRepositoryKey().equals(MavenPlugin.REPOSITORY_KEY)) {
        return true;
      }
    }
    return false;
  }

  protected abstract List<? extends MavenRule> getMavenRules();

  @Override
  public void execute(final MavenProject project, final SensorContext context) {

    this.project = project;
    this.context = context;

    // ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
    // Thread.currentThread().setContextClassLoader(new URLClassLoader(new URL[] {}, oldClassLoader));

    try {
      MavenSonarEmbedder.configure().
          usePomFile(project.getFile().getAbsolutePath()).
          build().run();
    } catch (MavenEmbedderException e) {
      throw new SonarException(e);
    }
    // Thread.currentThread().setContextClassLoader(oldClassLoader);
  }

  public MavenProject getProject() {
    return project;
  }

  protected Violation createViolation(final MavenRule mavenRule) {
    Rule rule = Rule.create(MavenPlugin.REPOSITORY_KEY, mavenRule.getKey());
    final File file = new File("", getProject().getFile().getName());
    // file.setLanguage(Xml.INSTANCE);
    return Violation.create(rule, file);
  }

  protected SensorContext getContext() {
    return context;
  }
}
