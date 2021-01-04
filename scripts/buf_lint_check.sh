#!/bin/sh

echo "********************************"
echo "Checking Protobuf file formatting"
echo "********************************"

../oppia-android-tools/buf-Darwin-x86_64 check lint --input=model/src/main/proto --input-config buf.yaml

status=$?

if [ "$status" = 0 ] ; then
  echo "Protobuf lint check completed successfully"
  exit 0
else
  echo "********************************"
  echo "Protobuf lint check issues found"
  echo "Please fix the above issues."
  echo "********************************"
  exit 1
fi
