#!/bin/bash

artifactName=wolm-catalog
outputDir=/Users/kmurray/.wolm/catalog
graphicsDir="$(dirname "$0")/src/grafart/S3"
s3Dir=s3://wordoflife.mn.catalog/

recompile=false
catalogParams=--for-upload
while [ "$1" ]; do
	case "$1" in
	--local)
		recompile=true
		catalogParams=${catalogParams/--for-upload/}
		;;
	*)
		echo "ERROR: unknown param '$1'" >>/dev/stderr
		exit 1
		;;
	esac
	shift
done

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
$recompile && recompile "$(dirname "$self")"

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

# prepare the output directory
[ "$outputDir" ] && {
	rm -rf "$outputDir"
	mkdir -p "$outputDir"
}

# run the package, passing through parameters
java -jar "$jar" $catalogParams --out=$outputDir "$@"

# sync the catalog files with S3
[[ "$catalogParams" == *--for-upload* ]] && {
	# NOTE: at one point, I tried to use the --size-only to minimize the number of 
	#       files uploaded, but then discovered that the catalog only generates files
	#       signed for 7 days, so we need to run this script every week, and upload
	#       all files to ensure that they are signed for the next week.
	#       Note that the signature of the URLs is required so that we can set the
	#       Content-Disposition to 'attachment' (even though the file is public).
	# UPDATE: Since then, I have removed the download link, so the signatures are no
	# longer required.
	echo "Syncing the files to S3 ..."
	aws --profile=wolm s3 sync --size-only --acl=public-read $graphicsDir/ $s3Dir &
	aws --profile=wolm s3 sync --size-only --acl=public-read $outputDir/ $s3Dir &
	wait
}

echo "Completed in $(( $(date +%s) - $startTime )) seconds"
#sleep 5

