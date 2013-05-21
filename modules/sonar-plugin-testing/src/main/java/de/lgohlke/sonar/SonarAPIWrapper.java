/*
 * sonar-mojo-bridge-testing
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
package de.lgohlke.sonar;

import com.thoughtworks.xstream.XStream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sonar.wsclient.Host;
import org.sonar.wsclient.Sonar;
import org.sonar.wsclient.connectors.HttpClient4Connector;
import org.sonar.wsclient.services.*;
import java.util.List;


/**
 * lightweight sonar api wrapper
 *
 * @author lars
 */
@RequiredArgsConstructor
public class SonarAPIWrapper {
  public interface Description {
    String getDescription();
  }

  @RequiredArgsConstructor
  public enum SCOPES implements Description {
    PRJ("project/module "),
    DIR(" directory (like Java package) "),
    FIL("file");

    @Getter
    private final String description;
  }

  @RequiredArgsConstructor
  public static enum QUALIFIERS implements Description {
    TRK("project"),
    BRC("module"),
    CLA("class"),
    UTS("unit test"),
    DIR("directory"),
    FIL("file");

    @Getter
    private final String description;
  }

  private final String sonarHost;
  private Query<? extends Model> lastQuery;

  public Resource getProjectWithKey(final String key) {
    ResourceQuery query = new ResourceQuery();
    query.setQualifiers("TRK");
    query.setScopes("PRJ");
    query.setResourceKeyOrId(key);
    lastQuery = query;
    return sonar().find(query);
  }

  public List<Violation> getViolationsFor(final Integer resourceId, final String... ruleKeys) {
    ViolationQuery query = new ViolationQuery(resourceId + "");
    query.setRuleKeys(ruleKeys);
    query.setDepth(-1);
    lastQuery = query;
    return sonar().findAll(query);
  }

  private Sonar sonar() {
    return new Sonar(new HttpClient4Connector(new Host(sonarHost)));
  }

  public void showQueryAndResult(final Model resource) {
    printOutXml(resource);
  }

  private void printOutXml(final Object resource) {
    System.out.println("query: " + sonarHost + lastQuery.getUrl());
    System.out.println(new XStream().toXML(resource));
  }

  public void showQueryAndResult(final List<? extends Model> resources) {
    printOutXml(resources);
  }

}
