#!/bin/bash

recompile=true
artifactName=wolm-catalog

realpath() {
	local p=$1
	while [ -L "$p" ]; do
		p=$(readlink "$p")
	done
	echo "$p"
}

findJar() {
	local self=$1

	# check same directory as this script
	ls "$(dirname "$self")"/${artifactName}-*-jar-with-dependencies.jar 2>/dev/null && return 0

	# check target directory
	ls "$(dirname "$self")"/target/${artifactName}-*-jar-with-dependencies.jar 2>/dev/null && return 0

	return 1
}

recompile() {
	local mvnHome=$1

	cd "$mvnHome"
	[ -f pom.xml ] && mvn package
	cd - >/dev/null
}

# find the original file
self=$(realpath "$0")

# recompile if necessary
[ "$recompile" ] && recompile "$(dirname "$self")"

# find the jar
jar=$(findJar "$self")
[ -f "$jar" ] || recompile "$(dirname "$self")"
jar=$(findJar "$self")
[ -f "$jar" ] || {
	echo "ERROR: Cannot find the ${artifactName} jar"
	exit 1
}

# start tracking time for later reporting
startTime=$(date +%s)

# run the package, passing through parameters
java -jar "$jar" --out=/Users/wolm/tmp --upload "$@"

echo "Completed in $(( $(date +%s) - $startTime )) seconds"
sleep 5
