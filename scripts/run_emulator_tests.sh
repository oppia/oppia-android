#!/bin/bash

# Enable job control (https://stackoverflow.com/a/46829294 for more).
set -m

#set -x

# Ensure all processes spawned by this task end at the end. See:
# https://spin.atomicobject.com/2017/08/24/start-stop-bash-background-process/.
#trap "exit" INT TERM ERR
#trap "kill 0" EXIT

echo "Using Android Home: $ANDROID_HOME"
echo "Building all instrumentation tests (debug)"
#./gradlew --full-stacktrace :app:compileDebugAndroidTestJavaWithJavac
echo "Running emulator tests"

# Establish directory for emulator test artifacts.
mkdir -pv ./emulator_tests

# TODO: update comment
# Output a list of all tests that are compatible with running on an Android
# emulator, with returned tests in fully qualified format (e.g
# org.oppia.app...) and separated by newlines.
#for test in $(find app/src/sharedTest -name "*Test.kt" | cut -d'/' -f5- | cut -d'.' -f1 | sed -e 's/\//./g') do
# Reference: https://stackoverflow.com/a/9612232
shopt -s globstar
echo "Using test whitelist: $TEST_WHITELIST"
for file_name in app/src/sharedTest/**/*Test.kt; do
  # https://stackoverflow.com/a/10987027
  stripped_file_name="${file_name#app/src/sharedTest/java/}"

  # https://stackoverflow.com/a/965072
  stripped_file_name_no_ext="${stripped_file_name%.*}"

  # https://stackoverflow.com/a/2871187
  qualified_test_name=${stripped_file_name_no_ext//\//.}
  test_directory_name=${stripped_file_name_no_ext//\//_}

  # https://stackoverflow.com/a/3162500
  test_name=${qualified_test_name##*.}

  # https://stackoverflow.com/a/20473191
  if [[ $TEST_WHITELIST =~ (^|[[:space:]])"$qualified_test_name"($|[[:space:]]) ]] ; then
    echo "Running $test_name"

    # Ensure any previous recordings are removed.
    adb shell rm "/sdcard/$test_name.mp4" &> /dev/null

    # https://unix.stackexchange.com/a/166486 (for stty -tostop).
    # stty -tostop; bash -m
    #$(./scripts/capture_screen_recording.sh "/sdcard/$test_name.mp4" < /dev/null) &
    (adb shell screenrecord "/sdcard/$test_name.mp4") &
    screen_record_pid=$!
    screen_record_android_pid=$(adb shell ps -A | grep record | awk '{print $2}')

    # Ensures that Gradle doesn't immediately stop after being backgrounded:
    # https://stackoverflow.com/a/17626350.
    (./gradlew --full-stacktrace :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class="$qualified_test_name" < /dev/null) &
    test_command_pid=$!
    # Wait a few seconds to make sure the Gradle command actually starts.
    sleep 2
    echo "Start waiting for $test_command_pid"
    wait $test_command_pid
    echo "Stop waiting for $test_command_pid"
    test_result=$?

    # Use interrupt to stop the recording to ensure the file is flushed and not
    # corrupted.
    echo "Killing task $screen_record_pid"
    #trap '' INT
    #(sleep 10; kill $screen_record_pid) &
    #kill -INT $screen_record_pid
    #wait $screen_record_pid
    #sleep 5
    #screen_record_job_id=$(jobs | grep capture_screen_recording | sed -r "s/^\[([0-9]+)\].+?$/\1/g")
    #jobs
    #fg $screen_record_pid
    #jobs -l
    #echo "fg $screen_record_job_id"
    #fg $screen_record_job_id
    #wait $screen_record_pid
    #sleep 5
    #trap

    adb shell kill -INT $screen_record_android_pid
    wait $screen_record_pid

    # Record artifacts to upload.
    mkdir -pv "./emulator_tests/$test_directory_name"
    echo "Checking test result: $test_result"
    if [ $test_result -eq 0 ]; then
      echo "Downloading"
      adb pull "/sdcard/$test_name.mp4" "./emulator_tests/$test_directory_name/"
    fi

    # Always download the test result reports.
    cp app/build/reports/androidTests/connected/index.html "./emulator_tests/$test_directory_name/"
    cp "app/build/reports/androidTests/connected/$qualified_test_name.html" "./emulator_tests/$test_directory_name/"
  else
    echo "Skipping disabled test $test_name"
  fi
done

#./gradlew --full-stacktrace :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.oppia.app.splash.SplashActivityTest
