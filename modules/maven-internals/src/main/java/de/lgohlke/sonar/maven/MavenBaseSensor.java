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

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.project.MavenProject;
import org.sonar.api.batch.maven.MavenPluginHandler;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.batch.scan.maven.MavenPluginExecutor;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: lars
 */
@Data
@Slf4j
public abstract class MavenBaseSensor<T extends ResultTransferHandler> extends MavenBaseSensorNG {
    private final RulesProfile rulesProfile;
    private final MavenPluginExecutor mavenPluginExecutor;
    private final MavenProject mavenProject;
    @Getter
    private BridgeMojoMapper<T> mojoMapper;
    @Getter(AccessLevel.PROTECTED)
    private final ResourcePerspectives resourcePerspectives;
    @Getter(AccessLevel.PROTECTED)
    private final Settings settings;

    @SuppressWarnings("unchecked")
    public MavenBaseSensor(final RulesProfile rulesProfile, final MavenPluginExecutor mavenPluginExecutor, final MavenProject mavenProject, ResourcePerspectives resourcePerspectives, Settings settings) {
        super(log, mavenProject, rulesProfile, resourcePerspectives, settings);

        this.rulesProfile = rulesProfile;
        this.mavenPluginExecutor = mavenPluginExecutor;
        this.mavenProject = mavenProject;
        this.resourcePerspectives = resourcePerspectives;
        this.settings = settings;

        checkNotNull(getClass().getAnnotation(SensorConfiguration.class), "each sensor must have the annotation " + SensorConfiguration.class);
        checkNotNull(getClass().getAnnotation(Rules.class), "each sensor must have the annotation " + Rules.class);

        SensorConfiguration configuration = getClass().getAnnotation(SensorConfiguration.class);
        verifyConfiguration(configuration);

        Class<? extends BridgeMojo<T>> bridgeMojoClass = (Class<? extends BridgeMojo<T>>) configuration.bridgeMojo();
        try {
            T resultTransferHandler = (T) configuration.resultTransferHandler().newInstance();
            mojoMapper = new BridgeMojoMapper<T>(bridgeMojoClass, resultTransferHandler);
        } catch (InstantiationException e) {
            throw new IllegalStateException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void verifyConfiguration(SensorConfiguration configuration) {
        final String identifier = configuration.mavenBaseIdentifier();
        checkArgument(identifier.length() > 0, "base identifier should not be empty");
        checkArgument(identifier.matches("[^:]+:[^:]+:[^:]+:"), "base identifier should match 'group:artifact:version:' (excluding goal)");
    }

    public boolean shouldExecuteOnProject(final Project project) {
        boolean isActivated = super.shouldExecuteOnProject(project);

        if (isActivated) {
            if (MavenPluginExecutorProxyInjection.checkIfIsMaven3(mavenPluginExecutor)) {
                MavenPluginExecutorProxyInjection.inject(mavenPluginExecutor, getClass().getClassLoader(), mojoMapper);
                return true;
            } else {
                MavenBaseSensor.log.warn("this plugin is incompatible with maven2, run again with maven3");
            }
        }

        return false;
    }

    @Override
    public MavenPluginHandler getMavenPluginHandler(final Project project) {
        String baseIdentifier = getClass().getAnnotation(SensorConfiguration.class).mavenBaseIdentifier();
        return MavenPluginHandlerFactory.createHandler(baseIdentifier + mojoMapper.getGoal());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
