#!/bin/sh

echo "********************************"
echo "Checking Java file formatting"
echo "********************************"

lint_results=$(java -jar ../oppia-android-tools/checkstyle-8.37-all.jar -c /google_checks.xml app/src/ data/src/ domain/src/ utility/src/ testing/src/ 2>&1)
lint_command_result=$?
echo $lint_results
if [ "$lint_command_result" -ne 0 ] || [ -z "$lint_results" ]; then
  # Assume any lint output or non-zero exit code is a failure.
  echo "********************************"
  echo "Checkstyle issue found."
  echo "Please fix the above issues."
  echo "********************************"
  exit 1
else
  echo "Checkstyle lint check completed successfully"
  exit 0
fi
