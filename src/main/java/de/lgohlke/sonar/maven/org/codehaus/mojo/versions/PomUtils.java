package de.lgohlke.sonar.maven.org.codehaus.mojo.versions;

import com.google.common.base.Joiner;
import org.fest.util.Preconditions;

/**
 * User: lars
 */
public final class PomUtils {

  private static final String DEPENDENCY_START = "<dependency>";
  private static final String DEPENDENCY_END = "</dependency>";

  private PomUtils(){}

  public static int getLine(String source, ArtifactUpdate artifactUpdate) throws Exception {
      Preconditions.checkNotNull(source);

      String[] lines = source.split("\\r?\\n");

      String groupd = "<groupId>" + artifactUpdate.getDependency().getGroupId() + "</groupId>";
      String artifact = "<artifactId>" + artifactUpdate.getDependency().getArtifactId() + "</artifactId>";
      String version = "<version>" + artifactUpdate.getDependency().getVersion() + "</version>";

      for (int i = 0; i < lines.length; i++) {
        if (lines[i].contains(version)) {
          if (containsEntry(lines, i, groupd, artifact, version)) {
            return i + 1;
          }
        }
      }
      return 0;
    }

  private static boolean containsEntry(String[] lines, int currentPosition, String group, String artifact, String version) {

    int start = findDependencyStartOrEnd(lines, currentPosition, -1);
    int end = findDependencyStartOrEnd(lines, currentPosition, +1);

    String[] part = new String[end-start+1];
    for(int i=start; i <= end; i++){
      part[i-start] = lines[i];
    }

    String fullBlock = Joiner.on("").join(part);

    return fullBlock.contains(version) && fullBlock.contains(artifact) && fullBlock.contains(group);
  }

  private static int findDependencyStartOrEnd(String[] lines, int currentPosition, int step) {
    int i = currentPosition;
    int limitToSearch = 10;
    while (i > 0 && Math.abs(currentPosition - i) <= limitToSearch) {
      if (lines[i].contains(DEPENDENCY_START) || lines[i].contains(DEPENDENCY_END)) {
        return i;
      } else {
        i += step;
      }
    }
    return -1;
  }
}
