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
package de.lgohlke.sonar.maven;

import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import static org.fest.assertions.api.Assertions.fail;

public class MavenInjectIT {
  @Test
  public void test() throws Exception {
    String jdbcDriver = System.getProperty("jdbcDriver");
    String jdbcUrl = System.getProperty("jdbcUrl");

    String format = "mvn sonar:sonar -Dsonar.jdbc.url=%s -Dsonar.jdbc.driver=%s -DskipTests -Dsonar.skipDesign -Dsonar.dynamicAnalysis";
    String cmd = String.format(format,jdbcUrl, jdbcDriver);
    try {
      Process proc = Runtime.getRuntime().exec(cmd);
      BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));

      String line = null;
      while ((line = reader.readLine()) != null) {
        System.out.println(line);
      }
      proc.waitFor();

      if (proc.exitValue() > 0) {
        fail("sonar test run failed");
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }

  }
}
