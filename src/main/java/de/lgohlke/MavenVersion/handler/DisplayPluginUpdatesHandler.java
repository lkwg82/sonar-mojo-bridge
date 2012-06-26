package de.lgohlke.MavenVersion.handler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DisplayPluginUpdatesHandler extends UpdateHandler {
  final static String key = "The following plugin updates are available:";
  boolean show = false;

  private void processLine(final String line) {
    Pattern pattern = Pattern.compile("([^\\ ]*)[\\ .]*((\\d\\.?)+) -> ((\\d\\.?)+)");
    Matcher matcher = pattern.matcher(line.replaceFirst("[\\ ]*", ""));
    if (matcher.find()) {
      ArtifactUpdate update = new ArtifactUpdate();
      update.setArtifactId(matcher.group(1));
      update.setOldVersion(matcher.group(2));
      update.setNewVersion(matcher.group(4));
      getUpdates().add(update);
    } else {
      System.err.println(getClass() + " error matching line: " + line);
    }
  }

  @Override
  protected void handleInfoLine(final String line) {
    if (show) {
      show = line.length() > 0;

      if (show) {
        processLine(line);
      }
    }

    if (!show) {
      show = line.contains(key);
    }
  }
}
