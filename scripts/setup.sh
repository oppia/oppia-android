#!/usr/bin/env bash

# INSTRUCTIONS:
# This script will move the pre-commit hook from script folder to
# the .git/hooks folder
#
# Run the script from the oppia-android root folder:
#
#   bash scripts/setup.sh
#
# NOTE: this script should be run once after the initial codebase setup

# Move file from script folder to .git/hooks folder
mv scripts/pre-commit .git/hooks/
