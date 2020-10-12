#!/bin/bash

echo "Checking code formatting"

ktlint --android domain/src/**/*.kt utility/src/**/*.kt data/src/**/*.kt app/src/**/*.kt testing/src/**/*.kt

status=$?

if [ "$status" = 0 ] ; then
  echo "Lint completed successfully"
  exit 0
else
  echo 1>&2 "Ktlint issue found"
  exit 1
fi
