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

import com.google.common.base.Preconditions;
import org.apache.maven.lifecycle.LifecycleExecutor;
import org.apache.maven.project.MavenProject;
import org.sonar.batch.scan.maven.MavenPluginExecutor;
import org.sonar.plugins.maven.DefaultMavenPluginExecutor;

import java.lang.reflect.Method;

import static org.fest.reflect.core.Reflection.field;

public final class MavenPluginExecutorProxyInjection {
    private MavenPluginExecutorProxyInjection() {
    }

    public static void inject(final MavenPluginExecutor mavenPluginExecutor, final ClassLoader classLoader, final BridgeMojoMapper handler) {
        if (checkIfIsMaven3(mavenPluginExecutor)) {
            Maven3ExecutionProcess.decorate(mavenPluginExecutor, classLoader, handler);
        }
    }

    public static boolean checkIfIsMaven3(final MavenPluginExecutor mavenPluginExecutor) {
        LifecycleExecutor lifecycleExecutor = field("lifecycleExecutor").ofType(LifecycleExecutor.class).in(mavenPluginExecutor).get();
        DetectingMavenVersionMavenPluginExecutor detectingMavenVersionMavenPluginExecutor = new DetectingMavenVersionMavenPluginExecutor(lifecycleExecutor);
        detectingMavenVersionMavenPluginExecutor.concreteExecute(null, "dummy goal");
        return detectingMavenVersionMavenPluginExecutor.isMaven3();
    }

    /**
     * {@see org.sonar.plugins.maven.DefaultMavenPluginExecutor} for details on detecting maven2 or maven3
     */
    private static class DetectingMavenVersionMavenPluginExecutor extends DefaultMavenPluginExecutor {
        private boolean wasExecuted = false;
        private boolean isMaven3;

        public DetectingMavenVersionMavenPluginExecutor(LifecycleExecutor le) {
            super(le, null);
        }

        @Override
        public void concreteExecuteMaven2(Method executeMethod, MavenProject pom, String goal) {
            isMaven3 = false;
            wasExecuted = true;
        }

        @Override
        public void concreteExecuteMaven3(MavenProject pom, String goal) {
            isMaven3 = true;
            wasExecuted = true;
        }

        boolean isMaven3() {
            Preconditions.checkArgument(wasExecuted, "needs to be executed");
            return isMaven3;
        }
    }
}
