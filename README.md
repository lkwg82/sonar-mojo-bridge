sonar-maven-integration
=======================


# about #

like to have some checks on a maven project based on existing maven pluginsv(only maven3 support)

# changelog #

2013-03-31
- [versions] added line detection to sho correct line in pom.xml

# screenshot #

![screenshot](https://github.com/SonarCommunity/sonar-maven-checks/blob/master/src/main/docs/screenshot.png?raw=true "screenshot")

# links #
* CI   : https://sonarplugins.ci.cloudbees.com/job/maven-checks/
* Sonar: http://nemo.sonarsource.org/dashboard/index/org.codehaus.sonar-plugins:sonar-maven-checks
* jira : http://jira.codehaus.org/browse/SONARPLUGINS/component/15663
* snapshots: http://repository-sonarplugins.forge.cloudbees.com/snapshot/org/codehaus/sonar-plugins/sonar-maven-checks

## mojos integrated ##

* [versions:display-dependency-updates](http://mojo.codehaus.org/versions-maven-plugin/display-dependency-updates-mojo.html)
* [versions:display-plugin-updates](http://mojo.codehaus.org/versions-maven-plugin/display-plugin-updates-mojo.html)
* [versions:update-parent](http://mojo.codehaus.org/versions-maven-plugin/update-parent-mojo.html) (just show updates available)

## mojos/features planned ##
* [enforcer:enforce](http://maven.apache.org/plugins/maven-enforcer-plugin/enforce-mojo.html) ()
* need some configuration to exclude "org.codehaus.sonar:sonar-batch:3.2.1 has newer version available: 3.3-RC2 (found in Dependencies)"

# limitations #

* so far it does not run with maven2 (not planned to change)
* so far it is not compatible and maybe disturb SonarRunner analysis runs (known bug by design)

# roadmap #

## basics ##
* simple prototype (done)
* check with sonar (done)
* integrate with MavenPluginExecutor for maven3 (done)
* added integration test for plugin (done)
* add comprehensive integration test (done)
* <del>check way to do this with maven2</del>
* <del>publish two artifacts for maven2 and maven3 injection</del>
* check remote run, on teamcity cluster (done)
* publish, according to http://docs.codehaus.org/display/SONAR/Plugin+Hosting (on hold, not accepted due to use of maven plugins inside plugin, see full discussion http://sonar.markmail.org/message/5xw7huebckrsopp7?q=list:org%2Ecodehaus%2Esonar%2Edev+maven-checks)
* try using the Mojos directly (done)

# ideas #
* checks for http://maven.apache.org/developers/conventions/code.html#POM_Code_Convention

# development #
clone repositoriy and dont forget to add lombok for your ide (http://projectlombok.org/download.html)
