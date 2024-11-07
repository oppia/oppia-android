#!/bin/bash

source scripts/formatting.sh

echo "********************************"
echo "Checking code formatting"
echo "********************************"

github_actions_path=$1

jar_file_path=$?

if [ $# -eq 0 ]; then
    jar_file_path="../oppia-android-tools/ktlint"
else
    jar_file_path="$github_actions_path/oppia-android-tools/ktlint"
fi

java -jar $jar_file_path --android app/src/**/*.kt data/src/**/*.kt domain/src/**/*.kt testing/src/**/*.kt utility/src/**/*.kt scripts/src/**/*.kt instrumentation/src/**/*.kt

status=$?

if [ "$status" = 0 ] ; then
  echo_success "Lint completed successfully."
  exit 0
else
  echo "********************************"
  echo_error "Ktlint issue found."
  echo "Please fix the above issues.
  You can also use the java -jar $jar_file_path -F --android domain/src/**/*.kt utility/src/**/*.kt data/src/**/*.kt app/src/**/*.kt testing/src/**/*.kt scripts/src/**/*.kt  instrumentation/src/**/*.kt
  command to fix the most common issues."
  echo_warning "Please note, there might be a possibility where the above command will not fix the issue.
  In that case, you will have to fix it yourself."
  echo "********************************"
  exit 1
fi
