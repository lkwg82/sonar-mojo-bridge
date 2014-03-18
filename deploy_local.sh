#!/bin/sh

sonarVersion=sonar-3.7.4
sonarDir=/home/lars/development/tools/$sonarVersion
sonarBin=$sonarDir/bin/linux-x86-64/sonar.sh
pluginDir=$sonarDir/extensions/plugins/
baseName=sonar-mojo-bridge-*.jar

rm -v $pluginDir/$baseName
mv -v target/$baseName $pluginDir

$sonarBin restart &
tail -f $sonarDir/logs/*log
