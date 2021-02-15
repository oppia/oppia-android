#!/bin/bash

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

committed_files=`git diff --name-only HEAD^ HEAD -- '***.kt'`

if [ -z "$committed_files" ]; then
    echo "No kotlin files have been committed, skipping ktlinter."
else
    echo "Checking only committed files."
    java -jar $jar_file_path --android $committed_files
fi

status=$?

if [ "$status" = 0 ] ; then
  echo "Lint completed successfully."
  exit 0
else
  echo "********************************"
  echo "Ktlint issue found."
  echo "Please fix the above issues.
  You can also use the ktlint -F --android domain/src/**/*.kt utility/src/**/*.kt data/src/**/*.kt app/src/**/*.kt testing/src/**/*.kt
  command to fix the most common issues."
  echo "Please note, there might be a possibility where the above command will not fix the issue.
  In that case, you will have to fix it yourself."
  echo "********************************"
  exit 1
fi
