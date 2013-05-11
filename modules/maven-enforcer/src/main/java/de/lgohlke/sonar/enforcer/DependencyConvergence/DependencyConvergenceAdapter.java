/*
 * sonar-maven-checks-maven-enforcer
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
package de.lgohlke.sonar.enforcer.DependencyConvergence;

import de.lgohlke.sonar.enforcer.ConfigurableEnforceMavenPluginHandler;
import de.lgohlke.sonar.enforcer.EnforcerRule;
import lombok.Getter;
import lombok.Setter;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactCollector;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.plugin.testing.SilentLog;
import org.apache.maven.plugins.enforcer.DependencyConvergence;
import org.apache.maven.plugins.enforcer.utils.DependencyVersionMap;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.tree.DefaultDependencyTreeBuilder;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilderException;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

/**
 * User: lars
 */
public class DependencyConvergenceAdapter extends DependencyConvergence implements EnforcerRule<DependencyConvergenceViolationAdapter> {
  @Setter
  private boolean uniqueVersions;
  @Getter
  @Setter
  private DependencyConvergenceViolationAdapter violationAdapter;

  @Override
  public void execute(EnforcerRuleHelper helper) throws EnforcerRuleException {
    try {
      DependencyNode node = getNode(helper);
      DependencyVersionMap visitor = new DependencyVersionMap(helper.getLog());
      visitor.setUniqueVersions(uniqueVersions);
      node.accept(visitor);

      violationAdapter.setErrors(visitor.getConflictedVersionNumbers());
    } catch (Exception e) {
      throw new EnforcerRuleException(e.getLocalizedMessage(), e);
    }
  }

  /**
   * Uses the {@link EnforcerRuleHelper} to populate the values of the
   * {@link org.apache.maven.shared.dependency.tree.DependencyTreeBuilder#buildDependencyTree(org.apache.maven.project.MavenProject, org.apache.maven.artifact.repository.ArtifactRepository, org.apache.maven.artifact.factory.ArtifactFactory, org.apache.maven.artifact.metadata.ArtifactMetadataSource, org.apache.maven.artifact.resolver.filter.ArtifactFilter, org.apache.maven.artifact.resolver.ArtifactCollector)}
   * factory method. <br/>
   * This method simply exists to hide all the ugly lookup that the {@link EnforcerRuleHelper} has to do.
   *
   * @param helper
   * @return a Dependency Node which is the root of the project's dependency tree
   * @throws EnforcerRuleException
   */
  private DependencyNode getNode(EnforcerRuleHelper helper) throws EnforcerRuleException {
    try {
      MavenProject project = (MavenProject) helper.evaluate("${project}");
      ArtifactFactory factory = (ArtifactFactory) helper.getComponent(ArtifactFactory.class);
      DefaultDependencyTreeBuilder dependencyTreeBuilder = new DefaultDependencyTreeBuilder();
      dependencyTreeBuilder.enableLogging(new SilentLog());

      ArtifactRepository repository = (ArtifactRepository) helper.evaluate("${localRepository}");
      ArtifactMetadataSource metadataSource = (ArtifactMetadataSource) helper.getComponent(ArtifactMetadataSource.class);
      ArtifactCollector collector = (ArtifactCollector) helper.getComponent(ArtifactCollector.class);
      ArtifactFilter filter = null; // we need to evaluate all scopes
      DependencyNode node = dependencyTreeBuilder.buildDependencyTree(project, repository, factory, metadataSource, filter,
          collector);
      return node;
    } catch (ExpressionEvaluationException e) {
      throw new EnforcerRuleException("Unable to lookup an expression " + e.getLocalizedMessage(), e);
    } catch (ComponentLookupException e) {
      throw new EnforcerRuleException("Unable to lookup a component " + e.getLocalizedMessage(), e);
    } catch (DependencyTreeBuilderException e) {
      throw new EnforcerRuleException("Could not build dependency tree " + e.getLocalizedMessage(), e);
    }
  }

  /**
   * we need to configure the rules via xml
   *
   * @param handler
   */
  @Override
  public void configure(final ConfigurableEnforceMavenPluginHandler handler) {
    handler.setParameter("rules/DependencyConvergence", null);
  }

  @Override
  public Class<DependencyConvergenceViolationAdapter> getViolationAdapterClass() {
    return DependencyConvergenceViolationAdapter.class;
  }
}
