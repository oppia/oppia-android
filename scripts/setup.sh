#!/bin/sh

# INSTRUCTIONS:
# This script will move the pre-push hook from script folder to
# the .git/hooks folder
#
# Run the script from the oppia-android root folder:
#
#   bash scripts/setup.sh
#
# NOTE: this script should be run once after the initial codebase setup

# Move file from script folder to .git/hooks folder
cp scripts/pre-push.sh .git/hooks/pre-push

# Create a folder where all the set up files will be downloaded
mkdir -p ../oppia-android-tools
cd ../oppia-android-tools

# Download ktlint
KTLINT="0.37.1"
echo Using Ktlint $KTLINT
curl -sSLOC - https://github.com/pinterest/ktlint/releases/download/$KTLINT/ktlint
chmod a+x ktlint
echo Ktlint file downloaded

# Download checkstyle
CHECKSTYLE="8.37"
echo Using Checkstyle version $CHECKSTYLE
curl -sSLOC - https://github.com/checkstyle/checkstyle/releases/download/checkstyle-$CHECKSTYLE/checkstyle-$CHECKSTYLE-all.jar
chmod a+x checkstyle-$CHECKSTYLE-all.jar
echo Checkstyle file downloaded
