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

bash scripts/setup_deps.sh

# Create a folder where all the set up files will be downloaded
mkdir -p ../oppia-android-tools
cd ../oppia-android-tools

# Download ktlint
bash ../oppia-android/scripts/ktlint_download.sh

# Download checkstyle
bash ../oppia-android/scripts/checkstyle_download.sh
