/*
 * sonar-mojo-bridge-maven-license
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
package com.excelsisnet.sonar.plugin.licenses;

import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metrics;

import java.util.Arrays;
import java.util.List;

public final class LicenseMetrics implements Metrics {

  public static final Metric LICENSE = new Metric.Builder("deplic", "dependency_license", Metric.ValueType.DATA)
      .setDescription("Dependency Licenses")
      .setDirection(Metric.DIRECTION_NONE)
      .setQualitative(false)
      .setDomain(CoreMetrics.DOMAIN_GENERAL)
      .create();

  public List<Metric> getMetrics() {
    return Arrays.asList(LICENSE);
  }

}
