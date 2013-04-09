sonar-maven-integration
=======================


# about #

like to have some checks on a maven project based on existing maven plugins (only for maven3)

presentation in German on this plugin here http://lkwg82.github.io/projects/2013.04.08-is24-sonar-maven-checks/

# changelog #

2013-04-08
- [versions] added white-/blacklisting for updates of artifacts to be notified of
2013-03-31
- [versions] added line detection to show correct line in pom.xml

# screenshots #

![screenshot](https://github.com/SonarCommunity/sonar-maven-checks/blob/master/screenshots/sonar_1.png?raw=true "screenshot")
![screenshot](https://github.com/SonarCommunity/sonar-maven-checks/blob/master/screenshots/sonar_2.png?raw=true "screenshot")
![screenshot](https://github.com/SonarCommunity/sonar-maven-checks/blob/master/screenshots/sonar_3.png?raw=true "screenshot")
![screenshot](https://github.com/SonarCommunity/sonar-maven-checks/blob/master/screenshots/sonar_4.png?raw=true "screenshot")

# links #
* Codehaus : http://docs.codehaus.org/display/SONAR/Sonar+Maven+Checks
* CI       : https://sonarplugins.ci.cloudbees.com/job/maven-checks/
* Sonar    : http://nemo.sonarsource.org/dashboard/index/org.codehaus.sonar-plugins:sonar-maven-checks
* jira     : http://jira.codehaus.org/browse/SONARPLUGINS/component/15663
* snapshots: http://repository-sonarplugins.forge.cloudbees.com/snapshot/org/codehaus/sonar-plugins/sonar-maven-checks

# download #

as long as not official sonar plugin, please download latest from /releases

## mojos integrated ##

* [versions:display-dependency-updates](http://mojo.codehaus.org/versions-maven-plugin/display-dependency-updates-mojo.html)
* [versions:display-plugin-updates](http://mojo.codehaus.org/versions-maven-plugin/display-plugin-updates-mojo.html)
* [versions:update-parent](http://mojo.codehaus.org/versions-maven-plugin/update-parent-mojo.html) (just show updates available)

## mojos/features planned (maybe) ##
* [enforcer:enforce](http://maven.apache.org/plugins/maven-enforcer-plugin/enforce-mojo.html) ()

# limitations #

* so far it does not run with maven2 (not planned to change)

# roadmap #

* try to get membership as official sonar plugin
* keep compatible with latest sonar
* accept user input ;)

# development #
clone repositoriy and dont forget to add lombok for your ide (http://projectlombok.org/download.html)
