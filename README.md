sonar-maven-integration
=======================


# about #

like to have some checks on a maven project, represented by its pom.xml (only maven3 support)

![screenshot](https://github.com/SonarCommunity/sonar-maven-checks/blob/master/src/main/docs/screenshot.png?raw=true "screenshot")

# links #
* CI   : https://sonarplugins.ci.cloudbees.com/job/maven-checks/
* Sonar: http://nemo.sonarsource.org/dashboard/index/org.codehaus.sonar-plugins:sonar-maven-checks
* jira : http://jira.codehaus.org/browse/SONARPLUGINS/component/15663

## mojos integrated ##

* [versions:display-dependency-updates](http://mojo.codehaus.org/versions-maven-plugin/display-dependency-updates-mojo.html)
* [versions:display-plugin-updates](http://mojo.codehaus.org/versions-maven-plugin/display-plugin-updates-mojo.html)

## mojos/features planned ##
* [versions:update-parent](http://mojo.codehaus.org/versions-maven-plugin/update-parent-mojo.html) (just show updates available)
* [enforcer:enforce](http://maven.apache.org/plugins/maven-enforcer-plugin/enforce-mojo.html) ()
* need some configuration to exclude "org.codehaus.sonar:sonar-batch:3.2.1 has newer version available: 3.3-RC2 (found in Dependencies)"

# roadmap #

## basics ##
* simple prototype (done)
* check with sonar (done)
* integrate with MavenPluginExecutor for maven3 (done)
* added integration test for plugin (done)
* add comprehensive integration test (done)
* <del>check way to do this with maven2</del>
* <del>publish two artifacts for maven2 and maven3 injection</del>
* check remote run, on teamcity cluster (in progress)
* publish, according to http://docs.codehaus.org/display/SONAR/Plugin+Hosting (in progress)
* try using the Mojos directly (done)

# ideas #
* checks for http://maven.apache.org/developers/conventions/code.html#POM_Code_Convention

# development #
clone repositoriy and dont forget to add lombok for your ide (http://projectlombok.org/download.html)