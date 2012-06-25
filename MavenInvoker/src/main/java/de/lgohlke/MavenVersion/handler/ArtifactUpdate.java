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
