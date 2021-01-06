#!/bin/sh

echo "********************************"
echo "Checking Java file formatting"
echo "********************************"

java -jar ../oppia-android-tools/checkstyle-8.37-all.jar -c=checkstyle.xml app/src/ data/src/ domain/src/ utility/src/ testing/src/

status=$?

if [ "$status" = 0 ] ; then
  echo "Checkstyle lint check completed successfully"
  exit 0
else
  echo "********************************"
  echo "Checkstyle issue found."
  echo "Please fix the above issues."
  echo "********************************"
  exit 1
fi
