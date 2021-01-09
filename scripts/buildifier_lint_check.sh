#!/bin/sh

echo "********************************"
echo "Checking Bazel file formatting"
echo "********************************"

../oppia_android_tools/buildifier --lint=warn --mode=check --warnings=-native-android,+out-of-order-load,+unsorted-dict-items -r app data domain model testing utility tools BUILD.bazel WORKSPACE oppia_android_test.bzl

status=$?

if [ "$status" = 0 ] ; then
  echo "Bazel lint check completed successfully."
  exit 0
else
  echo "********************************"
  echo "Buildifier issue found."
  echo "Please fix the above issues.
  You can also use the buildifier --lint=fix --warnings=-native-android,+out-of-order-load,+unsorted-dict-items -r app data domain model testing utility tools BUILD.bazel WORKSPACE oppia_android_test.bzl
  command to fix the most common reformat issues."
  echo "Please note, there might be a possibility where the above command will not fix the issue.
  In that case, you will have to fix it yourself."
  echo "********************************"
  exit 1
fi
