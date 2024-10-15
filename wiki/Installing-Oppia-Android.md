This wiki page explains how to install Oppia Android on your local machine. If you run into any issues with the installation process, please feel free to ask on [GitHub Discussions](https://github.com/oppia/oppia-android/discussions/categories/q-a-installation), so that we can help you while also making these instructions better for other developers. Thanks!

**Note:** Once you have set up the app locally, you might want to contribute code to the repository. Please follow our [onboarding instructions](https://github.com/oppia/oppia-android/wiki/Contributing-to-Oppia-android#onboarding-instructions) to get started!

## Table of Contents

- [Prepare developer environment](#prepare-developer-environment)
- [Install oppia-android](#install-oppia-android)
- [Run the app from Android Studio](#run-the-app-from-android-studio)
- [Set up and Run tests](#set-up-and-run-tests)
  - [Step-by-Step guidance for setting up and running app modules robolectric test](#step-by-step-guidance-for-setting-up-and-running-app-modules-robolectric-test)
  - [For tests that are in non-app modules, such as **domain** or **utility**:](#for-tests-that-are-in-non-app-modules-such-as-domain-or-utility)


## Prepare developer environment

1. Download/Install [Android Studio Bumblebee | Patch 3](https://developer.android.com/studio/archive).

   **Note**: We recommend installing **Android Studio Bumblebee | 2021.1.1 Patch 3** because newer versions of Android Studio[ do not support running tests where shared source sets are used](https://issuetracker.google.com/issues/232007221#comment18), a configuration we use at Oppia.

   **Direct download Url**: [Windows](https://redirector.gvt1.com/edgedl/android/studio/install/2021.1.1.23/android-studio-2021.1.1.23-windows.exe) | [Linux](https://redirector.gvt1.com/edgedl/android/studio/ide-zips/2021.1.1.23/android-studio-2021.1.1.23-linux.tar.gz) | [Intel Mac](https://redirector.gvt1.com/edgedl/android/studio/install/2021.1.1.23/android-studio-2021.1.1.23-mac.dmg) | [Apple Silicon Mac](https://redirector.gvt1.com/edgedl/android/studio/install/2021.1.1.23/android-studio-2021.1.1.23-mac_arm.dmg)

   You can have multiple versions of Android Studio installed on your machine at the same time.

2. Configure your Android Studio
   - In Android Studio, open Tools > SDK Manager.
     - In the "SDK Platforms" tab (which is the default), select `API Level 28` and also `API Level 30` (for Bazel support).
     - Also, navigate to the "SDK Tools" tab, click the "Show Package Details" checkbox at the bottom right, then click on "Android SDK Build-Tools 34-rc1" and select 32.0.0 (this is needed for Bazel support).

   - Then, click "Apply" to download and install these two SDKs/Tools.

   - Must have **JDK 11** selected:
     - In Android Studio, open Settings > Build, Execution, Deployment > Build Tools > Gradle and edit the Gradle JDK field.

## Install oppia-android

Please follow these steps to set up Oppia Android on your local machine.

1. Create a new, empty folder called `opensource/` within your home folder. Navigate to it (`cd opensource`), then [fork and clone](https://github.com/oppia/oppia-android/wiki/Fork-and-Clone-Oppia-Android) the Oppia-Android repo. This will create a new folder named `opensource/oppia-android`. Note that contributors who have write access to the repository may either create branches directly on oppia/oppia-android or use a fork.

   **Note**: Please keep the folder name as `oppia-android`. Changing the project folder name might lead to future issues with running the pre-push checks on your machine.

2. Run the `bash scripts/setup.sh` script, which adds some development tools for Oppia Android (ktlint, checkstyle, etc.): These tools perform pre-push checks to ensure that your code is properly formatted as per Oppia standards.

    - For Mac or Linux
       1. Open a terminal and navigate to `opensource/oppia-android/`.
       2. Run the script `bash scripts/setup.sh`.

    - For Mac with Apple M1 chip
       1. Locate Terminal in Finder.
       2. Right-click and create a duplicate Terminal (and rename it accordingly, say Terminal x86, to avoid confusion).
       3. In the Terminal x86, right-click and click "Get info", and check the option "Open using Rosetta".
       4. Navigate to `opensource/oppia-android/` in Rosetta.
       5. Finally, run `bash scripts/setup.sh` in Terminal x86 and all the required files should be generated. (You should see messages like `Ktlint file downloaded`, etc.)

    - For Windows
       1. Install [Git Bash Command Line](https://gitforwindows.org/)

          **Note**: When installing Git bash, check the option to add it to powershell, so that you can run bash commands from within powershell.
       2. Open Git Bash Command Line.
       3. Navigate to `opensource\oppia-android\`.
       4. Run the script `bash scripts/setup.sh`.
       5. Download the [google_checks.xml](https://github.com/checkstyle/checkstyle/blob/14005e371803bd52dff429904b354dc3e72638c0/src/main/resources/google_checks.xml) file. To do this, you can simply right-click on the download button at the top-right of the file.
       6. Copy this file to the directory where Git is installed (usually C:/Program Files/Git/).

3. In Android Studio, select `File > Open`, navigate to `opensource/oppia-android/`, and click `OK` to load the project.

4. Select the Compile JDK version
   - Go to Android Studio > Settings > Build, Execution, Deployment > Build Tools > Gradle
   - Under "**Gradle JDK**", select Android Studio Default JDK 11.
   - Then, click "Apply" and "OK" to complete your setup.
5. Click the elephant icon in the toolbar ("Sync Gradle") to ensure that all the correct dependencies are downloaded. (In general, you'll want to do this step any time you update your dependencies.)

## Run the app from Android Studio

1. Go to Tools > AVD Manager, click "Create Virtual Device...". Then:

   - Select a preferred device definition. In general, any device is fine, but you can use Pixel 3a as a default (if you're developing for phones) or Nexus 7 (if you're developing for tablets). After selecting a device, click "Next" at the bottom right to continue.
   - Select a system image (in general, API Level 28, unless you're an M1 Mac user, in which case use API Level 29). Then click "Next".
   - Click "Finish" to complete setup.

2. To run the app, select the emulator device you want from the dropdown menu to the left of the "Run" button in the toolbar.

3. Finally, click the "Run" button.

## Set up and run tests
Testing the app is an integral part of our development process. You will need to test all code changes to ensure that the app works correctly, therefore it is important to ensure that your test configuration works.

We strongly recommend running tests on Robolectric which is faster because it does not require a physical device or emulator setup.

### Configure Robolectric Tests

#### Step-by-Step guidance for setting up and running app modules robolectric test:

1. Go to **Edit Configuration** in Android Studio (Bumblebee | 2021.1.1 Patch 3)
   ![](https://user-images.githubusercontent.com/9396084/79109714-83525980-7d96-11ea-99d7-f83ea81a8a50.png)

2. Click on Add(+) -> **JUnit**
   ![](https://github.com/oppia/oppia-android/assets/76530270/87caf3fc-37d9-472d-92fd-b8ec49fb6b49)

3. Enter following information:
   - a) Name of test. Example: In my case "SplashActivityTest"
   - b) Make sure select "java 11" and oppia-android.app
   - c) Class path of Test class. Example: In my case "org.oppia.android.app.splash.
   SplashActivityTest"
   - d) Press `OK` to select the test.
   ![](https://github.com/oppia/oppia-android/assets/76530270/5901624a-df76-4b27-8f31-6077a68fcb89)

4. Click on "Run" button to run robolectric test. (In my case "SplashActivityTest")
   ![](https://github.com/oppia/oppia-android/assets/76530270/75a6b998-90c5-4f0a-8886-78f96970be90)

#### For tests that are in non-app modules, such as **domain** or **utility**::

1. In Android Studio, open the desired test file, e.g., `AnalyticsControllerTest`.
2. In the test file, to the left of the class name, click on the orange and green arrow, and select **Run 'AnalyticsControllerTest'**.
   - You will notice that the emulator is greyed out, but the run window will open to show the running tests:
   ![](https://user-images.githubusercontent.com/59600948/272657015-158117e5-47d2-40fc-a38b-5dee6c347556.png)

### Configure Emulator Tests

**Espresso is slower for running tests, so we recommend using Robolectric.**

1. In Android Studio, open the desired test file, e.g., `HomeActivityTest`.
2. In the Android Studio toolbar, click on the `Available Devices` option. Select an emulator that has between API 28-30.

   **Note**: If you don't have any available devices in this list, please follow [these instructions](#run-the-app-from-android-studio) to create one.

3. In the test file, to the left of the class name, click on the orange and green arrow, and select **Run 'HomeActivityTest'**.
   ![](https://user-images.githubusercontent.com/59600948/272657131-96e5354b-13a9-4709-969a-b9494a65c30f.png)

4. An "**Edit Configuration**" dialog will show up, and you should add the following settings under the general tab:
   - For module, select **oppia-android.app**
   - For Test, select **Class**
   - For Instrumentation class, **org.oppia.android.testing.OppiaTestRunner**, will be selected by default.
   - For target, select the **Use the device/snapshot dropdown** option.
   - Verify that your setup looks like below:

     ![](https://user-images.githubusercontent.com/59600948/272657260-2e654891-61be-467a-8ebd-c997aa2abda6.png)
- Finally, Click the "Apply" and "Okay" buttons.
- You may need to repeat step (3) above to run the test with the new configuration.
- Subsequent runs of any app module tests will not require editing the configuration.
- This configuration will run all the tests in that class.
5. To run only a specific test in a file:
   - Search or scroll down to the desired test name, to the left of the test name, click on the run icon and select **Run '`test name`''**.

### Next Steps
- Congratulations, you are ready to work on your first issue! Take a look at our [good first issues](https://github.com/oppia/oppia-android/issues?q=is%3Aopen+is%3Aissue+label%3A%22good+first+issue%22+no%3Aassignee) and leave a comment with your suggested fix. A maintainer will assign you the issue and provide any necessary guidance.

- When you are ready to submit a PR, please follow [these instructions](https://github.com/oppia/oppia-android/wiki/Guidance-on-submitting-a-PR) on submitting a PR.

- To learn about how we write tests at oppia-android, please refer to: [Oppia Android Testing](https://github.com/oppia/oppia-android/wiki/Oppia-Android-Testing).

- To get started with Bazel, please see [Oppia-Bazel-Setup-Instructions](https://github.com/oppia/oppia-android/wiki/Oppia-Bazel-Setup-Instructions) and follow the instructions appropriate to your OS.

- If you run into any issues during your setup, search our [discussions](https://github.com/oppia/oppia-android/discussions) forum or start a new discussion.

- Please see  [Troubleshooting-Installation](https://github.com/oppia/oppia-android/wiki/Troubleshooting-Installation) for solutions to some common setup issues.
