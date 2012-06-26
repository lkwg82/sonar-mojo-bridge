sonar-maven-integration
=======================

# about #

like to have some checks on a maven project, represented by its pom.xml

# links #
* CI : https://sonarplugins.ci.cloudbees.com/job/maven-checks/


## first realized checks ##
* have a more recent versions of a dependency available
* have a more recent versions of a plugin available



# roadmap #

* simple prototype (done)
* check with sonar (remove worst violations)
* check remote run, on teamcity cluster
* publish, according to http://docs.codehaus.org/display/SONAR/Plugin+Hosting
* try using the Mojos directly
* adding enforcer maven-plugin


# ideas #
* checks for http://maven.apache.org/developers/conventions/code.html#POM_Code_Convention
