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

import com.google.common.base.Preconditions;
import de.lgohlke.sonar.Configuration;
import de.lgohlke.sonar.SonarExecutor;
import org.sonar.wsclient.SonarClient;
import org.sonar.wsclient.issue.IssueQuery;
import org.sonar.wsclient.issue.Issues;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;

import java.io.IOException;

public abstract class MavenITAbstract {
    private static final String SONAR_HOST = "http://localhost:9000";
    protected SonarExecutor executor;
    protected SonarClient api;

    @BeforeClass
    public void beforeAllTests() {
        String jdbcDriver = System.getProperty("jdbcDriver");
        String jdbcUrl = System.getProperty("jdbcUrl");

        executor =
                new SonarExecutor(jdbcDriver, jdbcUrl) //
                        .skipDesign() //
                        .skipDynamicAnalysis() //
                        .skipTests() //
                        .showMavenErrorWhileAnalysis() //
                        .showMavenOutputWhileAnalysis();

        System.getProperties().put(Maven3SonarEmbedder.MavenSonarEmbedderBuilder.M2_HOME, Maven3SonarEmbedderTestConfiguration.MAVEN_HOME);
    }

    public void initAPI() {
        api = SonarClient.create(SONAR_HOST);
    }

    protected void skipTestIfNotMaven3() throws IOException, InterruptedException {
        final String mavenVersion = executor.getMavenVersion();
        if (mavenVersion.startsWith("2")) {
            throw new SkipException("could not proceed, because these tests only support maven3");
        }
    }

    protected Issues getIssuesFor(final String projectKey, final String ruleKey) {
        Preconditions.checkNotNull(api, "please call initAPI() before each test");

        IssueQuery query = IssueQuery.create().resolved(false).rules(ruleKey).componentRoots(projectKey);
        return api.issueClient().find(query);
    }

    protected String createRuleKey(final String specificRuleKey) {
        return Configuration.REPOSITORY_KEY + ":" + specificRuleKey;
    }
}
