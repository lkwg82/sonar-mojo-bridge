package de.lgohlke.MavenVersion.sonar;

import org.apache.maven.project.MavenProject;
import org.sonar.api.batch.AbstractSourceImporter;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Java;
import org.sonar.api.resources.ProjectFileSystem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PomSourceImporter extends AbstractSourceImporter {

  private final MavenProject project;

  public PomSourceImporter(final MavenProject project) {
    super(Java.INSTANCE);
    this.project = project;
  }

  @Override
  protected void analyse(final ProjectFileSystem fileSystem, final SensorContext context) {
    List<File> files = new ArrayList<File>();
    List<File> dirs = new ArrayList<File>();
    // adding the pom.xml
    files.add(project.getFile());
    dirs.add(project.getFile().getParentFile());
    parseDirs(context, files, dirs, false, fileSystem.getSourceCharset());
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

}
