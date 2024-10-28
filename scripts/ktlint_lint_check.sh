#!/bin/bash

echo "********************************"
echo "Checking code formatting"
echo "********************************"

github_actions_path=$1

if [ $# -eq 0 ]; then
    jar_file_path="../oppia-android-tools/ktlint"
    custom_rules_path="../oppia-android-tools/custom-ktlint-rules.jar"
else
    jar_file_path="$github_actions_path/oppia-android-tools/ktlint"
    custom_rules_path="$github_actions_path/oppia-android-tools/custom-ktlint-rules.jar"
fi

java -jar "$custom_rules_path" --android app/src/**/*.kt data/src/**/*.kt
status=$?

if [ "$status" = 0 ]; then
  echo "Lint completed successfully."
  exit 0
else
  echo "********************************"
  echo "Ktlint issue found."
  echo "Please fix the above issues."
  echo "You can also use the following command to fix the most common issues:"
  echo "java -cp \"$jar_file_path:$custom_rules_path\" com.pinterest.ktlint.Main -F --android domain/src/**/*.kt utility/src/**/*.kt data/src/**/*.kt app/src/**/*.kt testing/src/**/*.kt scripts/src/**/*.kt instrumentation/src/**/*.kt"
  echo "Please note, there might be cases where this command does not fix the issue."
  echo "In that case, you will need to fix it manually."
  echo "********************************"
  exit 1
fi
