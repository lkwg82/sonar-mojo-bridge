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

import com.excelsisnet.sonar.plugin.licenses.ui.LicenseTableWidget;
import org.sonar.api.Extension;

import java.util.ArrayList;
import java.util.List;

public class LicenseExtensions {
  public static List<Class<? extends Extension>> getExtensions() {
    return new ArrayList<Class<? extends Extension>>() {{
      add(LicenseMetrics.class);
      add(LicensesSensor.class);
      add(LicenseTableWidget.class);
    }};
  }
}
