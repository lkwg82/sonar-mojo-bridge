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
package de.lgohlke.MavenVersion.handler;
public class ArtifactUpdate {
  @Override
  public String toString() {
    return "ArtifactUpdate [groupId=" + groupId + ", artifactId=" + artifactId + ", oldVersion=" + oldVersion + ", newVersion=" + newVersion + "]";
  }

  private String groupId;
  private String artifactId;
  private String oldVersion;
  private String newVersion;

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(final String groupId) {
    this.groupId = groupId;
  }

  public String getArtifactId() {
    return artifactId;
  }

  public void setArtifactId(final String artifactId) {
    this.artifactId = artifactId;
  }

  public String getOldVersion() {
    return oldVersion;
  }

  public void setOldVersion(final String oldVersion) {
    this.oldVersion = oldVersion;
  }

  public String getNewVersion() {
    return newVersion;
  }

  public void setNewVersion(final String newVersion) {
    this.newVersion = newVersion;
  }
}
