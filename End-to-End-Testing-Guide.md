## Overview

End-to-End tests test the app from an end user’s experience by simulating the real user scenario and validating the system under test and its components for integration and data integrity. 

These tests play a major role in publishing the app. They run on a real or emulated device to make sure that our code interacts with the Android environment as expected, providing confidence in the final application or a feature when it's finished.

End-to-End tests in Oppia-android are written using [UiAutomator](https://developer.android.com/training/testing/ui-automator).
These tests are written under the instrumentation module and don’t have Gradle support.

```
instrumentation/ -- android test binaries for each test suite.
`-- src
    |-- java
    |   `-- org
    |       `-- oppia
    |           `-- android
    |               `-- instrumentation
    |                   `-- application -- Test application and modules
    `-- javatests
        `-- org
            `-- oppia
                `-- android
                    `-- instrumentation -- Test suites for each part of the app

```

These tests are run using [Bazel](https://bazel.build/) and [ADB](https://developer.android.com/studio/command-line/adb).
Each test suite tests a particular part of the app, each test suite has its own [kt_android_library](https://bazelbuild.github.io/rules_kotlin/kotlin#kt_android_library), [android_binary](https://docs.bazel.build/versions/main/be/android.html#android_binary) and [android_instrumentation_test](https://docs.bazel.build/versions/main/be/android.html#android_instrumentation_test).

Note: android_instrumentation_test target is not supported yet ([#3617](https://github.com/oppia/oppia-android/issues/3617) for details).

### How it works.
The android_binary of a test suite generates a test apk with the same name as the class of the test suite. This test apk is installed along with the original apk in the emulator. We use the adb [am instrument](https://developer.android.com/studio/test/command-line#AMSyntax) command to run the test on the target device or emulator.

### How to run an End-to-End test

#### Prerequisites:
1. [Set up Bazel for Oppia](https://github.com/oppia/oppia-android/wiki/Oppia-Bazel-Setup-Instructions#installation)
2. Add adb to the environment (platform-tools) i.e, add the following line to the .bashrc or the file path (note that this is platform dependent):
    ```
    export PATH=/home/<username>/Android/Sdk/platform-tools:$PATH
    ``` 
3. download and install [test-services-1.1.0.apk](https://mvnrepository.com/artifact/androidx.test.services/test-services/1.1.0) and [orchestrator-1.1.0.apk](https://mvnrepository.com/artifact/androidx.test/orchestrator/1.1.0) in the emulator. (run command in the directory where both apk are downloaded)
    ```
    adb install -r test-services-1.1.0.apk && adb install -r orchestrator-1.1.0.apk
    ```

4. java version 8 (Optional, only for uiautomatorviewer)
    ```
    java -version
    ```
    *output:*
    openjdk version "1.8.0_292"
    OpenJDK Runtime Environment (build 1.8.0_292-8u292-b10-0ubuntu1~18.04-b10)
    OpenJDK 64-Bit Server VM (build 25.292-b10, mixed mode)

#### Steps to run the tests

1. Build the oppia_test and test suite android_binary from the instrumentation module
    ```
    bazel build :oppia_test && bazel build //instrumentation:<test suite classname>
    ```
    e.g.:
    ```
    bazel build :oppia_test && bazel build //instrumentation/src/javatests/org/oppia/android/instrumentation/player:ExplorationPlayerTestBinary
    ```
2. install the oppia_test.apk and the test suite’s APK
    ```
    adb install -r bazel-bin/instrumentation/oppia_test.apk && adb install -r bazel-bin/instrumentation/<test suite classname>.apk
    ```
    e.g.:
    ```
    adb install -r bazel-bin/oppia_test.apk && adb install -r bazel-bin/instrumentation/src/javatests/org/oppia/android/instrumentation/player/ExplorationPlayerTestBinary.apk
    ```
3. Run the instrumentation tests using am instrument command
    ```
    adb shell 'CLASSPATH=$(pm path androidx.test.services) app_process / \
    androidx.test.services.shellexecutor.ShellMain am instrument -w -e clearPackageData true \
    -e targetInstrumentation org.oppia.android.app.instrumentation/androidx.test.runner.AndroidJUnitRunner \
    androidx.test.orchestrator/.AndroidTestOrchestrator'
    ```
    **Note:** "-e clearPackageData true" is used to clear your app's data in between runs. 

### Best practices when writing End-to-End tests
1. Each test suite should use the macro [oppia_instrumentation_test](https://github.com/oppia/oppia-android/blob/develop/instrumentation/oppia_instrumentation_test.bzl#L7) which generates the necessary targets required for each test suite.
2. Tests should follow the best practices for [writing good tests in Oppia Android](https://github.com/oppia/oppia-android/wiki/Oppia-Android-Testing), including ensuring that tests are focused on a single, high-level behavior that the suite wishes to verify as working (such as being able to play fully through an exploration).
3. Repetitive actions should be factored into helper methods. When these are generally useful for other end-to-end tests, they should be moved to [EndToEndTestHelper](https://github.com/oppia/oppia-android/blob/854071ab6adec35192be6d517ae16d2f3300ebb0/instrumentation/src/java/org/oppia/android/instrumentation/testing/EndToEndTestHelper.kt).
4. Prefer using existing helpers from ``EndToEndTestHelper`` rather than reimplementing them.
5. Prefer testing whole end-to-end flows rather than specific behaviors. For example: opening the app, downloading a topic, and being able to play it is an excellent end-to-end test since it’s verifying multiple cross-stack behaviors. Conversely, testing that a thumbnail is aligned correctly on the home screen is less useful and ought to be tested in Robolectric or Espresso local to the component showing that image.
6. Use uiautomatorviewer to get details of each view such as resource ID, content description, class, and other properties.

### Writing E2E tests
Unlike Robolectric and Espresso, tests in UiAutomator don't share the same code as Espresso and Robolectric. UiAutomator tests are dependent on [UiDevice](https://developer.android.com/reference/androidx/test/uiautomator/UiDevice). UiDevice provides access to all the views and gives possibilities to stimulate all UserActions in the device/emulator including an app other than the current app. 

In the instrumentation module all the UiAutomator tests are written using the Extensions of UiDevice in [EndToEndTestHelper.kt](https://github.com/oppia/oppia-android/blob/develop/instrumentation/src/java/org/oppia/android/instrumentation/testing/EndToEndTestHelper.kt)  

**Example:** 
To navigate from Profile screen to a Exploration page
```
    // device is a Instance of UiDevice.
    device.findObjectByRes("skip_text_view").click() // Click on the "skip" button on the onBoarding page.
    device.findObjectByRes("get_started_button").click() // Click on the "Getting Started' button.
    device.waitForRes("profile_select_text") // Waiting for the Profile Select screen to appear.
    device.findObjectByText("Admin").click() // Click on the admin profile.
    scrollRecyclerViewTextIntoView("First Test Topic") // Scroll to the view with text "First Test Topic.
    device.findObjectByText("First Test Topic").click() // Click on the "First Test Topic" text.
    device.findObjectByText("LESSONS").click() // Click on the "Lessons" tab.
    device.findObjectByText("First Story").click() // Click on the "First Story" tab.
    scrollRecyclerViewTextIntoView("Chapter 1: Prototype Exploration") // Scroll to first exploration.
    device.findObjectByText("Chapter 1: Prototype Exploration").click() // Click on the first exploration.
``` 