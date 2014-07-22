#!/bin/bash

# Find the wolm-podcast directory
# ... first try the script folder
cd "$(dirname "$0")"
if [ $(basename "$(pwd)") != wolm-catalog ]; then
	# ... try kevin's personal eclipse directory
	cd /Volumes/KMPersonal/eclipse-km/wolm-catalog || {
		echo "ERROR: cannot find the wolm-catalog directory"
		exit 1
	}
fi
startTime=$(date +%s)

# build the package if it doesn't exist
#test -s target/wolm-catalog-1.0-SNAPSHOT-jar-with-dependencies.jar || mvn package 1>&2 || exit 1
# DEBUG: always update the package
mvn package 1>&2 || exit 1

# run the package, passing through parameters
java -jar target/wolm-catalog-1.0-SNAPSHOT-jar-with-dependencies.jar --verbose "$@"

echo "Completed in $(( $(date +%s) - $startTime )) seconds" >>/dev/stderr
#sleep 5
