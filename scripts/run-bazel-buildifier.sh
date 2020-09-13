#!/usr/bin/env bash

# INSTRUCTIONS:
# This script runs a lint check on all the bazel files:
# WORKSPACE
# BUILD
# *.bzl

exec ./buildifier --lint=warn --mode=check --warnings=all -r app data domain model testing utility tools BUILD.bazel WORKSPACE oppia_android_test.bzl
echo "Bazel lint check completed successfully"
