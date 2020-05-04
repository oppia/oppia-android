#!/usr/bin/env bash

# INSTRUCTIONS:
# This script copies all oppia-assets to domain/src/main/assets folder in
# oppia-android project and also makes sure that they are not getting committed to github.
#
# Run the script from the oppia-android root folder:
#
#   bash scripts/copy_oppia_assets.sh <local_path_to_oppia_assets>
#
# NOTE: Once this script is run, we can build a signed APK.
# Make sure to run scripts/copy_dummy_assets.sh before committing any changes.

source_assets_path="$1"
destination_assets_path="domain/src/main/assets"

# Copy current assets files outside local repo
rsync -r "$destination_assets_path/" ../old-assets

# Remove current assets from domain/src/main/assets
rm -r $destination_assets_path/*

if [ "${source_assets_path: -1}" != "/" ]; then
    source_assets_path="$source_assets_path/"
fi

# Add .gitignore for the assets folder
if ! grep -q $destination_assets_path ".gitignore"; then
    touch .gitignore
    echo $destination_assets_path >> .gitignore
fi

# Populate new data inside assets folder
rsync -r $source_assets_path $destination_assets_path
