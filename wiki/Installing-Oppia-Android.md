This wiki page explains how to install Oppia Android on your local machine. If you run into any issues with the installation process, please feel free to ask on [GitHub Discussions](https://github.com/oppia/oppia-android/discussions/categories/q-a-installation), so that we can help you while also making these instructions better for other developers. Thanks!

**Note:** Once you have set up the app locally, you might want to contribute code to the repository. Please follow our [onboarding instructions](https://github.com/oppia/oppia-android/wiki/Contributing-to-Oppia-android#onboarding-instructions) to get started!

## Table of Contents

- [Prepare developer environment](#prepare-developer-environment)
- [Install oppia-android](#install-oppia-android)
- [Run the app from Android Studio](#run-the-app-from-android-studio)


## Prepare developer environment

1. Download/Install the latest version of [Android Studio](https://developer.android.com/studio/?gclid=EAIaIQobChMI8fX3n5Lb6AIVmH8rCh24JQsxEAAYASAAEgL4L_D_BwE&gclsrc=aw.ds#downloads). 

2. Download and Install **Java 8** using the links from [the Java website](https://www.java.com/en/download/).
   - **Note for Windows users:** Make sure to also set up the PATH system variable correctly for `Java`, following [these instructions](https://www.java.com/en/download/help/path.html).
   - [Instructions](https://www.java.com/en/download/help/linux_install.html) for Linux users.
   - [Instructions](https://www.java.com/en/download/help/mac_install.html) for Mac users.

3. In Android Studio, open Tools > SDK Manager.
   - In the "SDK Platforms" tab (which is the default), select `API Level 28` and also `API Level 31` (for Bazel support).
   - Also, navigate to the "SDK Tools" tab, click the "Show Package Details" checkbox at the bottom right, then click on "Android SDK Build-Tools 34-rc1" and select 29.0.2 (this is needed for Bazel support).

   Then, click "Apply" to download and install these two SDKs/Tools.

## Install oppia-android

Please follow these steps to set up Oppia Android on your local machine.

1. Create a new, empty folder called `opensource/` within your home folder. Navigate to it (`cd opensource`), then [fork and clone](https://github.com/oppia/oppia-android/wiki/Fork-and-Clone-Oppia-Android) the Oppia-Android repo. This will create a new folder named `opensource/oppia-android`. Note that contributors who have write access to the repository may either create branches directly on oppia/oppia-android or use a fork.

   **Note**: Please keep the folder name as `oppia-android`. Changing the project folder name might lead to future issues with running the pre-push checks on your machine.

2. Run the setup script, which adds some development tools for Oppia Android (ktlint, checkstyle, etc.):

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
       2. Open Git Bash Command Line.
       3. Navigate to `opensource/oppia-android/`.
       4. Run the script `bash scripts/setup.sh`.
       5. Download the [google_checks.xml](https://github.com/checkstyle/checkstyle/blob/14005e371803bd52dff429904b354dc3e72638c0/src/main/resources/google_checks.xml) file. To do this, you can simply right-click on the `Raw` button and click on `Save Link as`.
       6. Copy this file to the directory where Git is installed (usually C:/Program Files/Git/).

3. In Android Studio, select `File > Open`, navigate to `opensource/oppia-android/`, and click `OK` to load the project.

4. Click the elephant icon in the toolbar ("Sync Gradle") to ensure that all the correct dependencies are downloaded. (In general, you'll want to do this step any time you update your dependencies.)

## Run the app from Android Studio

1. Go to Tools > Device manager, click "Create Virtual Device...". Then:

   - Select a preferred device definition. In general, any device is fine, but you can use Pixel 3a as a default (if you're developing for phones) or Nexus 7 (if you're developing for tablets). After selecting a device, click "Next" at the bottom right to continue.
   - Select a system image (in general, API Level 28, unless you're an M1 Mac user, in which case use API Level 29). Then click "Next".
   - Click "Finish" to complete setup.

2. To run the app, select the emulator device you want from the dropdown menu to the left of the "Run" button in the toolbar.

3. Finally, click the "Run" button.
