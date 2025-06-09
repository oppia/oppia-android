#!/bin/bash

source scripts/formatting.sh

echo "********************************"
echo "Checking Bazel file formatting"
echo "********************************"

github_actions_path=$1

buildifier_file_path=$?

if [ $# -eq 0 ]; then
    buildifier_file_path="../oppia-android-tools/buildifier"
else
    buildifier_file_path="$github_actions_path/oppia-android-tools/buildifier"
fi

$buildifier_file_path --lint=warn --mode=check --warnings=-native-android,+out-of-order-load,+unsorted-dict-items -r app data domain instrumentation model testing utility third_party tools scripts BUILD.bazel WORKSPACE oppia_android_test.bzl

status=$?

if [ "$status" = 0 ] ; then
  echo_success "Buildifier lint check completed successfully"
  exit 0
else
  # Assume any lint output or non-zero exit code is a failure.
  echo "********************************"
  echo_error "Buildifier issue found."
  echo "Please fix the above issues."
  echo "********************************"
  exit 1
fi
