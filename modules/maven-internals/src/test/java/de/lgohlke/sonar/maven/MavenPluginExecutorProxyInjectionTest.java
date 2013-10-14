/*
 * sonar-mojo-bridge-maven-internals
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

import org.apache.maven.lifecycle.DefaultLifecycleExecutor;
import org.sonar.batch.scan.maven.MavenPluginExecutor;
import org.sonar.plugins.maven.DefaultMavenPluginExecutor;
import org.testng.annotations.Test;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * this is a border test<br/>
 * <p/>
 * Maven2 has a {@see LifecycleExecutor} with a method 'execute ' with 3 params <br/>
 * Maven3 has a {@see LifecycleExecutor} with a method 'execute ' with only one param <br/>
 * <p/>
 * {@see org.sonar.plugins.maven.DefaultMavenPluginExecutor} (Sonar 3.7.2)
 */
public class MavenPluginExecutorProxyInjectionTest {

    private static class Maven2LE extends DefaultLifecycleExecutor {
        public void execute(int a, int b, int c) {
        }
    }

    private static class Maven3LE extends DefaultLifecycleExecutor {
    }

    @Test
    public void testCheckIfIsMaven3Negative() throws Exception {
        MavenPluginExecutor mavenPluginExecutor = new DefaultMavenPluginExecutor(new Maven2LE(), null);
        assertThat(MavenPluginExecutorProxyInjection.checkIfIsMaven3(mavenPluginExecutor)).isFalse();
    }

    @Test
    public void testCheckIfIsMaven3Positiv() throws Exception {
        MavenPluginExecutor mavenPluginExecutor = new DefaultMavenPluginExecutor(new Maven3LE(), null);
        assertThat(MavenPluginExecutorProxyInjection.checkIfIsMaven3(mavenPluginExecutor)).isTrue();
    }
}
