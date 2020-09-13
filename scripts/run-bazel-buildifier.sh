#!/usr/bin/env bash

# INSTRUCTIONS:
# This script runs a lint check on all the bazel files:
# WORKSPACE
# BUILD
# *.bzl
#
# Run the script from the oppia-android root folder:
#
#       bash scripts/run-bazel-buildifier.sh
#
# NOTE: Once this script is run, we can safely push inorder to have successfully
# bazel lint check on GitHub Action, until this is shifted to pre-commit hook

exec buildifier --lint=warn --mode=check --warnings=all -r app data domain model testing utility BUILD.bazel WORKSPACE oppia_android_test.bzl