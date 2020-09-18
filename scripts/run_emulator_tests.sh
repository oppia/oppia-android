#!/bin/bash
#
# Runs all whitelisted Espresso-enabled tests, relying on the environmental variable TEST_WHITELIST
# to be defined (where the variable is a string with space-separated fully qualified test suite
# names that should be run). This script will run each test in sequence, noisily skipping ignored
# tests, and save both output logs and a video recording of the test.
#
# While this script is meant for Continuous Integration use, it can also be used locally for a
# specific test like so:
#   TEST_WHITELIST="org.oppia.app.splash.SplashActivityTest" bash ./scripts/run_emulator_tests.sh
#
# As the name implies, this is intended to be used with an emulator. However, it may also work with
# a real device as it just relies on ADB. The script does not expose a way to select which ADB
# device to use, so if multiple devices are present then this script will not work.

echo "Using Android Home: $ANDROID_HOME"
echo "Building all instrumentation tests (debug)"
./gradlew --full-stacktrace :app:compileDebugAndroidTestJavaWithJavac

# Establish directory for emulator test artifacts.
EMULATOR_TESTS_DIRECTORY=./emulator_test_output
mkdir -pv $EMULATOR_TESTS_DIRECTORY

test_suite_status=0

# Iterate over all tests that are compatible with running on an emulator. Reference:
# https://stackoverflow.com/a/5247919. Note that the Bash 4.0 globstar option isn't used here since
# the OSX version running on GitHub actions does not support this setting.
echo "Running emulator tests using test whitelist: $TEST_WHITELIST"
for file_name in $(find app/src/sharedTest -name "*Test.kt"); do
  # First, remove the shared test directory reference. See: https://stackoverflow.com/a/10987027.
  stripped_file_name="${file_name#app/src/sharedTest/java/}"

  # Second, remove the .kt extension. See: https://stackoverflow.com/a/965072.
  stripped_file_name_no_ext="${stripped_file_name%.*}"

  # Third, compute the qualified test name (e.g. org.oppia.app.splash.SplashActivityTest) and the
  # test directory name for test artifacts (e.g. org_oppia_app_splash_SplashActivityTest). See:
  # https://stackoverflow.com/a/2871187.
  qualified_test_name=${stripped_file_name_no_ext//\//.}
  test_directory_name=${stripped_file_name_no_ext//\//_}

  # Finally, extract the test name (e.g. SplashActivityTest). See:
  # https://stackoverflow.com/a/3162500.
  test_name=${qualified_test_name##*.}

  # Check if this test is in the whitelist. See: https://stackoverflow.com/a/20473191.
  if [[ $TEST_WHITELIST =~ (^|[[:space:]])"$qualified_test_name"($|[[:space:]]) ]] ; then
    # If it's in the whitelist, start execution.
    echo "Running $test_name"

    scratch_directory=$EMULATOR_TESTS_DIRECTORY/scratch
    mkdir -p $scratch_directory

    # Ensure any previous video recordings are removed, including scratch work.
    adb shell rm /sdcard/scratch*.mp4 &> /dev/null
    adb shell rm /sdcard/record_command_pid.txt &> /dev/null

    # Start the video recording in the background and track its PID along with the PID of the screen
    # recording process running on Android. Note that the command is set up to record up to 10
    # videos, each with a default length of 3 minutes (for a combined total of 30 minutes of
    # recorded video). See https://android.stackexchange.com/a/124277 for a reference on using a
    # shell command to wrap all of the subsequent screenrecord processes into 1.
    (adb shell 'sh -c "echo $$ > /sdcard/record_command_pid.txt; \
     screenrecord --bit-rate=4000000 /sdcard/scratch0.mp4 && \
     screenrecord --bit-rate=4000000 /sdcard/scratch1.mp4 && \
     screenrecord --bit-rate=4000000 /sdcard/scratch2.mp4 && \
     screenrecord --bit-rate=4000000 /sdcard/scratch3.mp4 && \
     screenrecord --bit-rate=4000000 /sdcard/scratch4.mp4 && \
     screenrecord --bit-rate=4000000 /sdcard/scratch5.mp4 && \
     screenrecord --bit-rate=4000000 /sdcard/scratch6.mp4 && \
     screenrecord --bit-rate=4000000 /sdcard/scratch7.mp4 && \
     screenrecord --bit-rate=4000000 /sdcard/scratch8.mp4 && \
     screenrecord --bit-rate=4000000 /sdcard/scratch9.mp4"') &
    screen_record_pid=$!

    # Sleep briefly to provide some time for the pid output to work, then extract the PID of the
    # child process that can receive the interrupt to kill the current screenrecord child process.
    sleep 1
    parent_android_pid=$(adb shell cat /sdcard/record_command_pid.txt)
    screen_record_group_android_pid=$(adb shell ps -A | grep -E "[0-9]+ +$parent_android_pid" | awk '{print $2}')

    # Start the actual test. Note that the stdin redirection is needed to ensure that Gradle doesn't
    # immediately pause after being backgrounded. See: https://stackoverflow.com/a/17626350. Also
    # for reference on running a specific test: https://stackoverflow.com/a/42518783.
    (./gradlew --full-stacktrace :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class="$qualified_test_name" < /dev/null) &

    # Capture the test command and wait for it to complete.
    test_command_pid=$!
    wait $test_command_pid
    test_result=$?

    # Send an interrupt signal to the Android screen record process (if it's still running) to
    # signal that recording should stop. Note that an interrupt can't be sent to the Unix process
    # blocking on adb since there's no way in Bash to send an interrupt to a background process, and
    # foregrounding it first will trigger the interrupt to be passed to its parent (this script),
    # killing it. Workarounds are challenging, and this approach is much simpler.
    adb shell kill -INT $screen_record_group_android_pid

    # Although the parent was interrupted, the child also needs to be intterupted. Interrupting the
    # parent prevents the next process from starting after this one is terminated.
    final_screen_record_android_pid=$(adb shell ps -A | grep screenrecord | awk '{print $2}')
    adb shell kill -INT $final_screen_record_android_pid

    # Wait for the screen record proces to finish writing the file before downloading it.
    wait $screen_record_pid

    files_to_combine=$(adb shell ls -1 /sdcard/scratch*.mp4)
    rm $scratch_directory/*.mp4 &> /dev/null
    rm $scratch_directory/video_list.txt &> /dev/null

    # Iterate over all files to download. See https://superuser.com/a/284226 for bash specifics.
    while separator= read -r file; do
      # https://stackoverflow.com/a/965072.
      video_file_name="${file##*/}"
      # https://stackoverflow.com/a/3915075.
      adb pull $file "$scratch_directory/$video_file_name"
      local_video_file_path=$(realpath "$scratch_directory/$video_file_name")
      echo "file '$local_video_file_path'" >> $scratch_directory/video_list.txt
    done <<< "$files_to_combine"

    # Make sure the final combined file doesn't currently exist.
    rm "$EMULATOR_TESTS_DIRECTORY/$test_directory_name/$test_name.mp4" &> /dev/null

    mkdir -pv "$EMULATOR_TESTS_DIRECTORY/$test_directory_name"

    # https://stackoverflow.com/a/11175851 for how the video concatenation works.
    # https://superuser.com/a/1363938 for quieting ffmpeg.
    ffmpeg -v quiet -f concat -safe 0 -i $scratch_directory/video_list.txt -c copy $scratch_directory/combined.mp4
    # Per https://trac.ffmpeg.org/wiki/ChangingFrameRate, change frame rate to 3 to reduce file
    # size.
    ffmpeg -v quiet -i $scratch_directory/combined.mp4 -filter:v fps=fps=3 "$EMULATOR_TESTS_DIRECTORY/$test_directory_name/$test_name.mp4"

    # Scratch directory cleanup.
    rm -r $scratch_directory &> /dev/null

    # Record artifacts to upload: an overview index of tests run for this test suite, and the
    # specific suite's results.
    cp app/build/reports/androidTests/connected/index.html "$EMULATOR_TESTS_DIRECTORY/$test_directory_name/"
    cp "app/build/reports/androidTests/connected/$qualified_test_name.html" "$EMULATOR_TESTS_DIRECTORY/$test_directory_name/"

    if [ $test_result -ne 0 ]; then
      # If any tests fail, make sure the overall test suite fails.
      test_suite_status=1
    fi
  else
    echo "Skipping disabled test $test_name"
  fi
done

exit $test_suite_status
