#!/bin/bash

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
bash ../oppia-android/scripts/ktlint_download.sh

# Download checkstyle
bash ../oppia-android/scripts/checkstyle_download.sh

# Download buf
bash ../oppia-android/scripts/buf_download.sh

# Add protobuf platform for M1 Mac
arch_name="$(uname -m)"
if [ "${arch_name}" = "x86_64" ]; then
    if [ "$(sysctl -in sysctl.proc_translated)" = "1" ]; then
        echo '\nprotobuf_platform=osx-x86_64' >> ../oppia-android/local.properties

        echo 'Added protobuf platform to local properties.'
    fi
fi
