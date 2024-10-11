#!/bin/bash

source scripts/formatting.sh

echo "********************************"
echo "Checking Java file formatting"
echo "********************************"

github_actions_path=$1

jar_file_path=$?

if [ $# -eq 0 ]; then
    jar_file_path="../oppia-android-tools/checkstyle-8.37-all.jar"
else
    jar_file_path="$github_actions_path/oppia-android-tools/checkstyle-8.37-all.jar"
fi

lint_results=$(java -jar $jar_file_path -c /google_checks.xml app/src/ data/src/ domain/src/ utility/src/ testing/src/ scripts/src/ 2>&1)

lint_command_result=$?

echo $lint_results

if [ "$lint_command_result" -ne 0 ] || [ -z "$lint_results" ] || [[ ${lint_results} == *"[WARN]"* ]]; then
  # Assume any lint output or non-zero exit code is a failure.
  echo "********************************"
  echo_error "Checkstyle issue found."
  echo "Please fix the above issues."
  echo "********************************"
  exit 1
else
  echo_success "Checkstyle lint check completed successfully"
  exit 0
fi
