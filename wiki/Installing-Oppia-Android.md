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

1. Download/Install the latest version of Android Studio from [this location](https://developer.android.com/studio).

2. Configure your Android Studio
   - In Android Studio, open Tools > SDK Manager.
     - In the "SDK Platforms" tab (which is the default), select `API Level 29` and also `API Level 30`.
     - Also, navigate to the "SDK Tools" tab, click the "Show Package Details" checkbox at the bottom right, then click on "Android SDK Build-Tools 34-rc1" and select 32.0.0 (this is needed for Bazel support).

   - Then, click "Apply" to download and install these two SDKs/Tools.

## Install Bazel

**Select your Operating System for instructions on setting up Bazel:**

- [For Windows/Ubuntu/Fedora](https://github.com/oppia/oppia-android/wiki/Bazel-Setup-Instructions-for-Windows)
- [For Mac including M1/M2](https://github.com/oppia/oppia-android/wiki/Bazel-Setup-Instructions-for-Mac)
- [For Linux](https://github.com/oppia/oppia-android/wiki/Bazel-Setup-Instructions-for-Linux)

## Install oppia-android

Please follow these steps to set up Oppia Android on your local machine.

1. Create a new, empty folder called `opensource/` within your home folder. Navigate to it (`cd opensource`), then [fork and clone](https://github.com/oppia/oppia-android/wiki/Fork-and-Clone-Oppia-Android) the Oppia-Android repo. This will create a new folder named `opensource/oppia-android`. Note that contributors who have write access to the repository may either create branches directly on oppia/oppia-android or use a fork.

   **Note**: Please keep the folder name as `oppia-android`. Changing the project folder name might lead to future issues with running the pre-push checks on your machine.

2. Run the `bash scripts/setup.sh` script, which adds some development tools for Oppia Android (ktlint, checkstyle, etc.). You should see a message indicating that the various tools have been downloaded.
   ![image](https://github.com/user-attachments/assets/d039f070-a842-4874-b534-01df39599ce6)  
   
   **Note** These tools perform important pre-push checks to ensure that your code is properly formatted as per Oppia standards.

    - **For Mac or Linux**
       1. Open a terminal and navigate to `opensource/oppia-android/`.
       2. Run the script `bash scripts/setup.sh`.

    - **For Windows**
       1. Install [Git Bash Command Line](https://gitforwindows.org/)

          **Note**: When installing Git bash, check the option to add it to powershell, so that you can run bash commands from within powershell.
       2. Open Git Bash Command Line.
       3. Navigate to `opensource\oppia-android\`.
       4. Run the script `bash scripts/setup.sh`.
       5. Download the [google_checks.xml](https://github.com/checkstyle/checkstyle/blob/14005e371803bd52dff429904b354dc3e72638c0/src/main/resources/google_checks.xml) file. To do this, you can simply right-click on the download button at the top-right of the file.
       6. Copy this file to the directory where Git is installed (usually C:/Program Files/Git/).

## Run the app from Android Studio

1. Launch Android Studio, and on the welcome screen, select **Plugins** to install the Bazel for Android Studio plugin and restart Android Studio.

2. On next open, click the options button next to the new project/open project to find the "Import Bazel Project" option.

   <img width="1029" alt="Image" src="https://github.com/user-attachments/assets/efcc4e6e-b6ba-44fc-9c96-aa381de83911" />

3. A setup wizard will open. For Workspace, click (**...**) to open file manager and select /opensource/oppia-android as the location since this is where we have our project's `WORKSPACE` file.

   ![Image](https://github.com/user-attachments/assets/9baf6f71-560b-49fe-84a7-c7b846a64287)

4. On **OK**, you will be asked to **Trust project**. Accept because we trust the project.

   <img width="1029" alt="Image" src="https://github.com/user-attachments/assets/bfc91f6d-d809-418d-ab20-89d899f12f72" />

5. On **next**, to Select Project View, **select Create from scratch**.

   <img width="1029" alt="Image" src="https://github.com/user-attachments/assets/cdc110d6-2926-4f96-b7d8-117e2c716f76" />

6. A file similar to the one below will be generated:

   <img width="760" alt="Image" src="https://github.com/user-attachments/assets/5a26210f-865d-4048-b5a5-73379984bfb5" />

7. In the above file, modify it so that it looks like below(copy and paste):
   
   ```
   directories:
      # Add the directories you want added as source here
      # By default, we've added your entire workspace ('.')
      .
      
   # Automatically includes all relevant targets under the 'directories' above
   derive_targets_from_directories: true
   
   targets:
   # If source code isn't resolving, add additional targets that compile it here
      //:oppia_dev_binary
      //app
      //app/src/main/...
      //domain/...
      //model/...
      //testing/...
      //utility/...
      //data/...
      //scripts/...
      
   additional_languages:
      # Uncomment any additional languages you want supported
      # c
      # dart
      # kotlin
      # java
      kotlin

   # Please uncomment an android-SDK platform. Available SDKs are:
      # android_sdk_platform: android-28
      android_sdk_platform: android-29
      # android_sdk_platform: android-30
      # android_sdk_platform: android-31
      # android_sdk_platform: android-32
      # android_sdk_platform: android-33
      android_sdk_platform: android-34
   
   shard_sync: true
   sync_flags:
      --local_ram_resources=HOST_RAM*.5
      --discard_analysis_cache
   
   java_language_level: 11
   ```
   **Note**: Only enable Android-SDK platforms that you have installed per the [Prepare developer environment](#prepare-developer-environment) section above. Android-SDK platforms are useful for creating emulators to test your app with.

8. Click **Create**, and allow the project to synchronize.

9. Once sync has finished, you can now build and install the app on either a virtual or physical device. Run the following commands in your terminal:
   
   On Sdk 29 and below, run:
   ```
   bazel mobile-install //:oppia_dev_binary
   ```
   This will build, install and launch the app on your device. 
   
   On Sdk 30 and newer, run:
   ```
   bazel build //:oppia_dev_binary
   ```
   followed by:
   ```
   adb install bazel-bin/oppia_dev_binary.apk  
   ```
   Starting from Sdk 30, incremental builds, like those executed using `bazel mobile-install`, are no longer permitted, necessitating the use of two separate commands.

 You can also run the project by using the Bazel plugin to set up run configurations for the target that you wish to build. This performs the same action as the commands above, but using the GUI to run the app might be more intuitive for some developers.

 ![Screenshot 2025-02-26 at 22 16 32](https://github.com/user-attachments/assets/f3be5288-dc96-4079-bcb2-b514de81f899)

   Edit configurations allows us to specify the run command:

   ![Image](https://github.com/user-attachments/assets/7270b6b3-11ac-4b49-a39a-734e0437bb9e)

   - We can specify the target **Name**, which is helpful for identifying the target from the list on the left.
   - **Target expression** requires a build target such as `//:oppia_dev_binary`.
   - **The Bazel command** is `mobile-install`
   - **Bazel flags** are optional.
   - Select **Apply** and then **Close** or **Ok**.

## Set up and run tests
Testing the app is an integral part of our development process. You will need to test all code changes to ensure that the app works correctly, therefore it is important to ensure that your test configuration works.

Our Bazel setup currently supports running tests on Robolectric which is fast because it does not require a physical device or emulator setup.

### Configure Robolectric Tests

#### Step-by-Step guidance for setting up and running app module robolectric tests:

1. In Android Studio, open the test file that you wish to run. If sync completed successfully when the project was opened, there will be a green run arrow next to the class name, as well as next to each individual test name. Clicking on the arrow will run either the full class or a single test as selected.

2. Because we have set up the Bazel plugin, we can also run a test class using the run arrow at the top of the project window:

   ![Screenshot 2025-02-26 at 22 16 32](https://github.com/user-attachments/assets/f3be5288-dc96-4079-bcb2-b514de81f899)

   Edit configurations allows us to specify the run command:
   ![Screenshot 2025-02-26 at 22 19 34](https://github.com/user-attachments/assets/462d07d2-4407-4898-bbc4-544509bd7486)

   - We can specify the test **name**, which is helpful for identifying the test target from the list on the left.
   - **Target expression** requires the fully qualified path to the test file.
   - **The Bazel command** for running tests is `test`
   - **Bazel flags** are optional.
   - Select **Apply** and then **Close** or **Ok**.

#### For tests that are in non-app modules, such as **domain** or **utility**::

1. In Android Studio, open the test file that you wish to run. If sync completed successfully when the project was opened, there will be a green run arrow next to the class name, as well as next to each individual test name. Clicking on the arrow will run either the full class or a single test as selected.

2. A second way to run a test class would be to open the `BUILD` file located in the same package as the test file, and clicking on the green arrow next to the test target name.
   ![Image](https://github.com/user-attachments/assets/c1ee9e23-b0b2-430a-9592-9f87a2fdcbf0)

3. Finally, for all module tests classes, we can run the tests from the terminal using the command:
   ```
   bazel test full-path-of-test-file
   ```
   e.g.
   ```
   bazel test domain/src/test/java/org/oppia/android/domain/onboarding/AppStartupStateControllerTest
   ```

### Next Steps
- Congratulations, you are ready to work on your first issue! Take a look at our [good first issues](https://github.com/oppia/oppia-android/issues?q=is%3Aopen+is%3Aissue+label%3A%22good+first+issue%22+no%3Aassignee) and leave a comment with your suggested fix. A maintainer will assign you the issue and provide any necessary guidance.

- When you are ready to submit a PR, please follow [these instructions](https://github.com/oppia/oppia-android/wiki/Guidance-on-submitting-a-PR) on submitting a PR.

- To learn about how we write tests at oppia-android, please refer to: [Oppia Android Testing](https://github.com/oppia/oppia-android/wiki/Oppia-Android-Testing).

- To learn more about getting started with Bazel, please see [Oppia-Bazel-Setup-Instructions](https://github.com/oppia/oppia-android/wiki/Oppia-Bazel-Setup-Instructions) and follow the instructions appropriate to your OS.

- If you run into any issues during your setup, search our [discussions](https://github.com/oppia/oppia-android/discussions) forum or start a new discussion.

- Please see  [Troubleshooting-Installation](https://github.com/oppia/oppia-android/wiki/Troubleshooting-Installation) for solutions to some common setup issues.
