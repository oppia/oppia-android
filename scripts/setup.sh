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

# Download ktlint
KTLINT="0.37.1"
echo Using Ktlint $KTLINT
mkdir -p ../oppia-android-tools
cd ../oppia-android-tools
curl -sSLOC - https://github.com/pinterest/ktlint/releases/download/$KTLINT/ktlint
chmod a+x ktlint
echo Ktlint file downloaded
