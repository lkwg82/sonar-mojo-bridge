package de.lgohlke.MavenVersion.handler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DisplayDependencyUpdatesHandler extends UpdateHandler {
  private final static String KEY_DEPENDENCIES = "The following dependencies in Dependencies have newer versions:";
  private final static String KEY_DEPENDENCY_MGMT = "The following dependencies in Dependency Management have newer versions:";
  private boolean show = false;
  private boolean longLine = false;

  private ArtifactUpdate update;

  @Override
  protected void handleInfoLine(final String line) {

    if (show) {
      show = line.length() > 0;

      if (show) {
        processLine(line);
      }
    }

    if (!show) {
      show = line.startsWith(KEY_DEPENDENCIES) || line.startsWith(KEY_DEPENDENCY_MGMT);
    }
  }

  private void processLine(final String line) {
    String VERSION_REGEX = "(\\d[^\\ ]*)";
    String GROUPID_REGEX = "([^:]*):";
    final String ARTIFACT_REGEX = "([^\\ ]*)[\\ .]*";
    final String trimmedLine = line.replaceFirst("[\\ ]*", "");

    final String VERSIONS_REGEX = VERSION_REGEX + " -> " + VERSION_REGEX;
    final String GROUP_ARTIFACT_REGEX = GROUPID_REGEX + ARTIFACT_REGEX;
    final String ONLINE_REGEX = GROUP_ARTIFACT_REGEX + VERSIONS_REGEX;

    if (longLine) {
      Pattern pattern = Pattern.compile(VERSIONS_REGEX);
      Matcher matcher = pattern.matcher(trimmedLine);
      if (matcher.find()) {
        update.setOldVersion(matcher.group(1));
        update.setNewVersion(matcher.group(2));
        getUpdates().add(update);
        longLine = false;
      } else {
        System.err.println(getClass() + " error matching [second] line: " + line);
      }
    } else {
      if (trimmedLine.matches(ONLINE_REGEX)) {

        Pattern pattern = Pattern.compile(ONLINE_REGEX);
        Matcher matcher = pattern.matcher(trimmedLine);
        matcher.find();
        update = new ArtifactUpdate();
        update.setGroupId(matcher.group(1));
        update.setArtifactId(matcher.group(2));
        update.setOldVersion(matcher.group(3));
        update.setNewVersion(matcher.group(4));
        getUpdates().add(update);
      } else if (trimmedLine.matches(GROUP_ARTIFACT_REGEX)) {
        Pattern pattern = Pattern.compile(GROUP_ARTIFACT_REGEX);
        Matcher matcher = pattern.matcher(trimmedLine);
        matcher.find();
        update = new ArtifactUpdate();
        update.setGroupId(matcher.group(1));
        update.setArtifactId(matcher.group(2));
        longLine = true;
      } else {
        System.err.println(getClass() + " error matching  line: " + line);
      }
    }
  }
}
