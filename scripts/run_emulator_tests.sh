#!/bin/bash
#
# Runs all whitelisted Espresso-enabled tests, relying on the environmental
# variable TEST_WHITELIST to be defined (where the variable is a string with
# space-separated fully qualified test suite names that should be run). This
# script will run each test in sequence, noisily skipping ignored tests, and
# save both output logs and a video recording of the test.
#
# While this script is meant for Continuous Integration use, it can also be used
# locally for a specific test like so:
#   TEST_WHITELIST="org.oppia.app.splash.SplashActivityTest" bash ./scripts/run_emulator_tests.sh
#
# As the name implies, this is intended to be used with an emulator. However, it
# may also work with a real device as it just relies on ADB. The script does not
# expose a way to select which ADB device to use, so if multiple devices are
# present then this script will not work.

echo "Using Android Home: $ANDROID_HOME"
echo "Building all instrumentation tests (debug)"
./gradlew --full-stacktrace :app:compileDebugAndroidTestJavaWithJavac

# Establish directory for emulator test artifacts.
EMULATOR_TESTS_DIRECTORY=../emulator_tests
mkdir -pv $EMULATOR_TESTS_DIRECTORY

# Iterate over all tests that are compatible with running on an emulator.
# Reference: https://stackoverflow.com/a/9612232
shopt -s globstar
echo "Running emulator tests using test whitelist: $TEST_WHITELIST"
for file_name in app/src/sharedTest/**/*Test.kt; do
  # First, remove the shared test directory reference. See:
  # https://stackoverflow.com/a/10987027.
  stripped_file_name="${file_name#app/src/sharedTest/java/}"

  # Second, remove the .kt extension. See: https://stackoverflow.com/a/965072.
  stripped_file_name_no_ext="${stripped_file_name%.*}"

  # Third, compute the qualified test name (e.g.
  # org.oppia.app.splash.SplashActivityTest) and the test directory name for
  # test artifacts (e.g. org_oppia_app_splash_SplashActivityTest). See:
  # https://stackoverflow.com/a/2871187.
  qualified_test_name=${stripped_file_name_no_ext//\//.}
  test_directory_name=${stripped_file_name_no_ext//\//_}

  # Finally, extract the test name (e.g. SplashActivityTest). See:
  # https://stackoverflow.com/a/3162500.
  test_name=${qualified_test_name##*.}

  # Check if this test is in the whitelist. See:
  # https://stackoverflow.com/a/20473191.
  if [[ $TEST_WHITELIST =~ (^|[[:space:]])"$qualified_test_name"($|[[:space:]]) ]] ; then
    # If it's in the whitelist, start execution.
    echo "Running $test_name"

    # Ensure any previous video recordings for this test are removed.
    adb shell rm "/sdcard/$test_name.mp4" &> /dev/null

    # Start the video recording in the background and track its PID along with
    # the PID of the screen recording process running on Android. Both will be
    # used after the test completes.
    (adb shell screenrecord "/sdcard/$test_name.mp4") &
    screen_record_pid=$!
    screen_record_android_pid=$(adb shell ps -A | grep record | awk '{print $2}')

    # Start the actual test. Note that the stdin redirection is needed to ensure
    # that Gradle doesn't immediately pause after being backgrounded. See:
    # https://stackoverflow.com/a/17626350. Also for reference on running a
    # specific test: https://stackoverflow.com/a/42518783.
    (./gradlew --full-stacktrace :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class="$qualified_test_name" < /dev/null) &

    # Capture the test command and wait for it to complete.
    test_command_pid=$!
    wait $test_command_pid

    # Send an interrupt signal to the Android screen record process (if it's
    # still running) to signal that recording should stop. Note that an
    # interrupt can't be sent to the Unix process blocking on adb since there's
    # no way in Bash to send an interrupt to a background process, and
    # foregrounding it first will trigger the interrupt to be passed to its
    # parent (this script), killing it. Workarounds are challenging, and this
    # approach is much simpler.
    adb shell kill -INT $screen_record_android_pid

    # Wait for the screen record proces to finish writing the file before
    # downloading it.
    wait $screen_record_pid

    # Record artifacts to upload: an overview index of tests run for this test
    # suite, the specific suite's results, and a video (up to 3 minutes long) of
    # the test run.
    mkdir -pv "$EMULATOR_TESTS_DIRECTORY/$test_directory_name"
    adb pull "/sdcard/$test_name.mp4" "$EMULATOR_TESTS_DIRECTORY/$test_directory_name/"
    cp app/build/reports/androidTests/connected/index.html "$EMULATOR_TESTS_DIRECTORY/$test_directory_name/"
    cp "app/build/reports/androidTests/connected/$qualified_test_name.html" "$EMULATOR_TESTS_DIRECTORY/$test_directory_name/"
  else
    echo "Skipping disabled test $test_name"
  fi
done
