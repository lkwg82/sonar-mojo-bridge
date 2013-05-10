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
package de.lgohlke.sonar.maven.org.apache.maven.plugins.enforcer;

import lombok.Setter;
import org.apache.maven.project.MavenProject;
import org.sonar.api.resources.File;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.Violation;
import org.sonar.plugins.xml.language.Xml;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * User: lgohlke
 */
public class DependencyConvergenceViolationAdapter extends ViolationAdapter<DependencyConvergenceRule> {
  @Setter
  private Collection<String> errors;

  public DependencyConvergenceViolationAdapter(MavenProject mavenProject) {
    super(mavenProject);
  }

  @Override
  public List<Violation> getViolations() {
    List<Violation> violations = new ArrayList<Violation>();

    File file = new File("", getMavenProject().getFile().getName());
    file.setLanguage(Xml.INSTANCE);

    Rule rule = getRule(DependencyConvergenceRule.class);
    for (String error : errors) {
      Violation violation = Violation.create(rule, file);
      violation.setLineId(1);
      violation.setMessage(error);
      violations.add(violation);
    }
    return violations;
  }
}
