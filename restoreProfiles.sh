#!/bin/bash

set -e 

function restore()
{
	curl -X POST -u admin:admin -F 'backup=@src/test/resources/'$1'' http://localhost:9000/api/profiles/restore
	echo
}

restore qualityProfile_testOldDependencies.xml
restore qualityProfile_testMissingMavenVersion.xml
restore qualityProfile_testOldParentPom.xml
restore qualityProfile_testDependencyConvergence.xml
