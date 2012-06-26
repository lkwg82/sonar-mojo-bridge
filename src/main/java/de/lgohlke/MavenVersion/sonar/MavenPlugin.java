package de.lgohlke.MavenVersion.sonar;

import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.PropertyType;
import org.sonar.api.SonarPlugin;
import org.sonar.plugins.xml.language.Xml;
import org.sonar.plugins.xml.language.XmlCodeColorizerFormat;

import java.util.Arrays;
import java.util.List;

@Properties({
  @Property(key = MavenPlugin.ANALYSIS_ENABLED,
    name = "enable maven analysis",
    description = "Enable maven analysis.",
    defaultValue = MavenPlugin.DEFAULT,
    global = true,
    project = true,
    type = PropertyType.BOOLEAN)})
public class MavenPlugin extends SonarPlugin {

  public static final String ANALYSIS_ENABLED = "sonar.maven.analysis";
  public static final String REPOSITORY_KEY = "maven";
  public static final String REPOSITORY_NAME = "Maven";
  public static final String DEFAULT = "true";

  @SuppressWarnings({"rawtypes", "unchecked"})
  public List getExtensions() {

    return Arrays.asList(
        MavenVersionSensor.class,
        RulesRepository.class,
        // xml language from xml-plugin
        Xml.class,
        // source importer
        PomSourceImporter.class,

        // code colorizer
        XmlCodeColorizerFormat.class);
  }

}
