#!/usr/bin/env bash

# INSTRUCTIONS:
# This script copies all dummy-assets to domain/src/main/assets folder and deletes
# the original oppia assets.
#
# Run the script from the oppia-android root folder:
#
#   bash scripts/copy_dummy_assets.sh
#
# NOTE: This script should only be run after running the scripts/copy_oppia_assets.sh

destination_assets_path="domain/src/main/assets"

# Remove current assets from domain/src/main/assets
rm -r domain/src/main/assets/*

# Copy old assets files to domain/src/main/assets
rsync -r ../old-assets/ domain/src/main/assets/

# Remove the assets folder available outside the repo.
rm -rf ../old-assets

# Remove .gitignore for the assets folder
grep -v $destination_assets_path ".gitignore" > temp && mv temp ".gitignore"
