This wiki page explains how to install Oppia Android on your local machine. If you run into any issues with the installation process, please feel free to ask on [GitHub Discussions](https://github.com/oppia/oppia-android/discussions/categories/q-a-installation), so that we can help you while also making these instructions better for other developers. Thanks!

**Note:** Once you have set up the app locally, you might want to contribute code to the repository. Please follow our [onboarding instructions](https://github.com/oppia/oppia-android/wiki/Contributing-to-Oppia-android#onboarding-instructions) to get started!

## Table of Contents

- [Prepare developer environment](#prepare-developer-environment)
- [Install Bazel](#install-bazel)
  - [Bazel Set up for Mac](#bazel-set-up-for-mac)
  - [Bazel Set up for Linux](#bazel-set-up-for-linux)
  - [Bazel Set up for Windows](#bazel-set-up-for-windows)
- [Install oppia-android](#install-oppia-android)
- [Opening the Project in Android Studio](#opening-the-project-in-android-studio)
- [Set up and Run tests](#set-up-and-run-tests)
  - [Running app module tests](#running-app-module-tests)
  - [Running non-app module tests](#running-non-app-module-tests)
- [Next Steps](#next-steps)
- [Troubleshooting Installation](#troubleshooting-installation)

## Prepare developer environment

1. Download and install Android Studio **Giraffe** from [this location](https://developer.android.com/studio/archive).
   **Note**: You may try newer or older versions of Android Studio, but we can't guarantee good support as we have not tested them extensively yet.

2. Configure your Android Studio
   - From your PC's application menu, open Android Studio. 
   - On the welcome screen, find the "more options menu" and select **SDK Manager**
     
     <img width="1029" alt="Image" src="https://github.com/user-attachments/assets/efcc4e6e-b6ba-44fc-9c96-aa381de83911" />
    
   - In the "SDK Platforms" tab (which is the default), select `API Level 30` and also `API Level 34`.
   - Also, navigate to the "SDK Tools" tab, click the "Show Package Details" checkbox at the bottom right, then click on "Android SDK Build-Tools 34-rc1" and select 32.0.0 (this is needed for Bazel support).

   - Then, click "Apply" to download and install these two SDKs/Tools.
     - On this screen, note the **Android SDK Location**, which is set as the ANDROID_HOME variable in the Bazel setup step below.

3. Prepare a test device
   You require a physical Android device or an Android emulator to run the Oppia app. 
   - Physical devices are useful for a real-user experience feel, and testing with [accessibility tools](https://github.com/oppia/oppia-android/wiki/Accessibility-A11y-Guide#setting-up-accessibility-scanner-and-talkback).
   
     **Set up a physical device for development**
      - On the device, open the **Settings** app, select **Developer options**, and then **enable USB debugging** (if applicable).
        - If you don't see **Developer options**, you might need to enable developer mode first. Follow [these instructions](https://developer.android.com/studio/debug/dev-options#enable) to do so.
        - Connect to your device using USB.
        - Verify that your device is connected by running the `adb devices` command from your terminal.
   
      - Emulators provide the flexibility of testing the application on a variety of devices and Android API levels without needing to have each physical device.
   
     **Set up an Emulator**
      - For the best experience, you should use the emulator in Android Studio on a computer with at least the following specs:
        - 16 GB RAM
        - 64-bit Windows 10 or higher, MacOS 12 or higher, Linux, or ChromeOS operating system
        - 16 GB disk space
     
      - If you don't have these specs, the emulator might still run but not smoothly. In this case, consider testing on a physical device.

      - On the welcome screen, find the "more options menu" and select **Virtual Device Manager**
     
        <img width="1029" alt="Image" src="https://github.com/user-attachments/assets/efcc4e6e-b6ba-44fc-9c96-aa381de83911" />
         
        * If you already have a project open in Android Studio, in the right hand toolbar, locate `Device Manager`.
      - Click the **+**, and then click **Create Virtual Device**.
      - Follow the wizard to create a phone and a tablet AVD, setting the **system image** to be one of `API Level 30` or `API Level 34` as installed above.
      - After creating your devices, you will be able to see a list of all the devices on the device manager panel.
      - For more information on the different configurations available for AVDs, please visit the [official documentation page.](https://developer.android.com/studio/run/managing-avds)

## Install Bazel
### Instructions

**The steps to setting up Bazel are:**
1. Install Bazel
2. Set Bazel and ANDROID_HOME paths permanently in your terminal
3. Verify that the build is working

The expected output of `bazel --version` once Bazel set up is successful is:

```
bazel 6.5.0
```

### Bazel Set up for Mac
It is recommended to use the binary installer steps outlined below. For other ways to install Bazel on Mac, refer to the [official page](https://bazel.build/install/os-x).

**Step 1: Install Xcode command line tools**

In macOS 14 or earlier, it is sufficient to install the Xcode command line tools package by using xcode-select:

```
xcode-select --install
```

Otherwise, for macOS 15 and later, you must have Xcode 6.1 or later installed on your system.

Download Xcode from the [App Store](https://apps.apple.com/us/app/xcode/id497799835) or the [Apple Developer site](https://developer.apple.com/download/more/?=xcode).

Once Xcode is installed, accept the license agreement for all users with the following command:

```
sudo xcodebuild -license accept
```

**Step 2: Download the Bazel installer**

Click [here](https://github.com/bazelbuild/bazel/releases/download/6.5.0/bazel-6.5.0-installer-darwin-x86_64.sh) to download the Bazel 6.5.0 binary installer.

**Step 3: Run the installer**

Run the Bazel installer as follows:

```shell
chmod +x "bazel-6.5.0-installer-darwin-x86_64.sh"
./bazel-6.5.0-installer-darwin-x86_64.sh --user
```

**Step 4: Set up your environment**

You must set the path for `Bazel` and `ANDROID_HOME` before running bazel build for oppia-android, otherwise you will get an error.

If you ran the Bazel installer with the `--user` flag as above, the Bazel executable is installed in your $HOME/bin directory. It’s a good idea to add this directory to your default paths, as follows:

```shell
export PATH="$PATH:$HOME/bin"
```

To set the `ANDROID_HOME` path permanently in your terminal run these commands:

```
sudo nano /etc/paths
```
- Enter your password, when prompted.
- Go to the bottom of the file, and enter this path: `$HOME/Library/Android/sdk`
- Hit control-x to quit.
- Enter “Y” to save the modified buffer.
- That’s it! To test it, in a new terminal window, type: `echo $PATH`

**Step 5: Verify that the build is working**

Run the following command in your terminal:

```
bazel --version
```

### Bazel Set up for Linux
This section covers Bazel installation on Ubuntu. 

Recommendations:
- use Ubuntu 18.04 (LTS), 20.04 (LTS), or 22.04 (LTS).
- use ``apt`` installation instructions.

- **Note**: if you encounter any errors related to `cURL`, please set up cURL on your machine using `sudo apt install curl`.

**Step 1: Add Bazel distribution URI as a package source**

This is a one-time setup step.

```
sudo apt install apt-transport-https curl gnupg -y
curl -fsSL https://bazel.build/bazel-release.pub.gpg | gpg --dearmor >bazel-archive-keyring.gpg
sudo mv bazel-archive-keyring.gpg /usr/share/keyrings
echo "deb [arch=amd64 signed-by=/usr/share/keyrings/bazel-archive-keyring.gpg] https://storage.googleapis.com/bazel-apt stable jdk1.8" | sudo tee /etc/apt/sources.list.d/bazel.list
```

**Step 2: Install and update Bazel**

Run:
```shell
sudo apt-get update && sudo apt-get install bazel-6.5.0
```

**Step 3: Set up your environment**

Ensure that your `ANDROID_HOME` environment variable is set to the location of your Android SDK. Assuming the SDK is installed to default locations, you can use the following commands to set the `ANDROID_HOME` variable:

```
export ANDROID_HOME=$HOME/Android/Sdk/
```

**Step 4: Verify that the build is working**

Run the following command in your terminal:

```
bazel --version
```

### Bazel Set up for Windows
This page outlines one way to allow Bazel to be used in CLI form on Windows. Please note that **this support is currently experimental**. We suggest that you post a discussion at [github-discussions](https://github.com/oppia/oppia-android/discussions/categories/q-a-installation) if you run into any problems.

Unlike Unix-based systems where Bazel runs natively without issue, the current solution on Windows is to install an Ubuntu-based subsystem. Windows currently only supports a terminal experience in this subsystem (though there is a prerelease version of the software with GUI support) which means Android Studio will not be supported. You will need to continue using the Windows version of Android Studio and only use the Linux subsystem for building & running Robolectric or JUnit-based tests.

Due to the issues mentioned above, we recommend dual-booting your PC with Linux for a smoother experience. However, if you prefer to use Windows, please follow the instructions below:

**Main prerequisites**:
- Windows 10+: These instructions are geared towards users of Windows 10+ (older versions will not be compatible). If you're using an older version of Windows, please follow up with a comment on [this issue](https://github.com/oppia/oppia-android/issues/3371).
- At least 4GB of free local disk storage space (for Linux & needed dependencies), but more will probably help when building the app

At a high-level, the steps to make Bazel work on Windows are:
1. Install the Ubuntu subsystem
2. Install prerequisite debian packages
3. Install the Android SDK
4. Install Bazel
5. Set up the environment to be able to build Oppia Android
6. Verify that the build is working
7. Run Bazel commands as needed during development

**Step 1: Install Ubuntu subsystem**

Please follow Microsoft's [setup instructions](https://docs.microsoft.com/en-us/windows/wsl/install-win10) to set up the Linux subsystem on Windows 10+. From there, you should install **Ubuntu** (the instructions below are based on the apt package manager on Ubuntu; other Linux distributions & package managers may work but they are untested by the team).

Once installed, open the Ubuntu terminal from the start menu.

From within the Ubuntu terminal, start by ensuring all packages are up-to-date:

```sh
sudo apt update && sudo apt upgrade
```

**Step 2: Install JDK 17+**

Setting up Bazel for Oppia Android requires JDK>=17 for [Android Package Manager](#3-installing-the-android-sdk).

For Ubuntu systems, this can be set up using:

```shell
sudo apt install openjdk-17-jdk
```

For Fedora 25+, this can be set up using:

```
sudo dnf install java-17-openjdk
```

**Step 3: Install GCC**

Install gcc using the following command:

```shell
sudo apt install gcc
```

**Step 4: Install the Android SDK**

We need to be able to run Linux-compatible Android utilities which requires installing a Linux version of the Android SDK. Since we can't install Android Studio in the subsystem, we need to do this via CLI commands. The steps below are extracted from [this article](https://proandroiddev.com/how-to-setup-android-sdk-without-android-studio-6d60d0f2812a).

First, prepare the environment for the SDK by creating the default directory to hold the SDK (from within Ubuntu terminal):

```sh
mkdir -p $HOME/Android/Sdk
```

Second, navigate to https://developer.android.com/studio#command-tools in a web browser (in Windows) and select to download the latest **Linux** command tools (even though you're using Windows, the Linux commandline tools are needed--the Windows version will not work with these instructions). Once downloaded, copy the zip file to the new SDK location (note that the ``/mnt/c/...`` path is based on ``C:\Users\<Name>\Downloads`` being the default download location--this may not be the case on your system) with your Windows username filled in for ``<Name>``:

```sh
cp /mnt/c/Users/<Name>/Downloads/commandlinetools*.zip $HOME/Android/Sdk
```

After that, change to the directory, unzip the archive, and remove it:

```sh
cd $HOME/Android/Sdk
unzip commandlinetools*.zip
rm commandlinetools*.zip
```

From there, the command line tools need to be moved in order to indicate to the tools themselves that they're relative to the Android SDK root directory:

```sh
cd cmdline-tools/
mkdir tools
mv -i * tools
cd ..
```

(The above may give a warning for the ``mv`` command since it will try moving ``tools`` into ``tools``--this can be ignored).

At this point, we can define the ``ANDROID_HOME`` variable to point to the new SDK root, and also update the ``PATH`` to point to cmdnline-tools so that we can actually install the SDK. To do this, run the following commands to append new lines to ``~/.bashrc``:

```sh
echo "export ANDROID_HOME=\$HOME/Android/Sdk" >> ~/.bashrc
echo "export PATH=\$ANDROID_HOME/cmdline-tools/tools/bin/:\$PATH" >> ~/.bashrc
source ~/.bashrc
```

(The last line reloads your Bash configuration file so that the variable adjustments above become live in your local terminal).

The ``sdkmanager`` command can now be used to install the necessary packages. Run each of the following commands in succession (you may need to accept licenses for the SDK packages in the same way you would when using Android Studio):

```sh
sdkmanager
sdkmanager --install "platform-tools"
sdkmanager --install "platforms;android-33"
sdkmanager --install "build-tools;32.0.0"
```

When the commands above are finished running, the Android SDK should now be installed in your subsystem & be accessible to Bazel.

**Step 5: Install Bazel**

Use the steps outlined for Ubuntu Linux [above](#bazel-set-up-for-linux).

For Fedora 25+
- Install Bazelisk instead of Bazel using the command below in Fedora:
```
wget https://github.com/bazelbuild/bazelisk/releases/download/v1.8.1/bazelisk-linux-amd64
chmod +x bazelisk-linux-amd64
sudo mv bazelisk-linux-amd64 /usr/local/bin/bazel
```

**Step 6: Verify that the build is working**

Run the following command in your terminal:

```
bazel --version
```

**Known limitations with using an Ubuntu subsystem on Windows:**
- Android Studio must run on native Windows: this is a current limitation. However, https://github.com/microsoft/wslg is a WIP project that may provide an alternative option which allows full development to take place within the subsystem.
- The subsystem is very slow: unfortunately, this is just a limitation with how the subsystem works on Windows. Until we fix the actual build pipeline to work natively, this is likely going to be a limitation that we have to live with. Note that installing an Ubuntu VM or dual-booting Ubuntu may lead to less issues & better performance than using a subsystem, but this hasn't yet been investigated or documented yet (see [#3437](https://github.com/oppia/oppia-android/issues/3437) for the WIP issue).
- ADB is limited within the subsystem and thus must be used from within a Windows CLI like Command Prompt, Powershell, or Git Bash (if it's installed) in order to deploy the Bazel-built test or APK binary to an emulator or real device
- Emulators likely cannot be launched from the subsystem (headless might be possible, but this hasn't been tested)

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

## Opening the project in Android Studio

1. Launch Android Studio, and on the welcome screen, select **Plugins** to install the Bazel for Android Studio plugin and restart Android Studio.

2. On next open, click the options button next to the new project/open project to find the "Import Bazel Project" option.

   <img width="1029" alt="Image" src="https://github.com/user-attachments/assets/efcc4e6e-b6ba-44fc-9c96-aa381de83911" />

3. A setup wizard will open. For Workspace, click (**...**) to open file manager and select `/opensource/oppia-android` as the location since this is where we have our project's `WORKSPACE` file.

   ![Image](https://github.com/user-attachments/assets/9baf6f71-560b-49fe-84a7-c7b846a64287)

4. On **OK**, you will be asked to **Trust project**. Accept because we trust the project.

   <img width="1029" alt="Image" src="https://github.com/user-attachments/assets/bfc91f6d-d809-418d-ab20-89d899f12f72" />

5. On **next**, to Select Project View, select **Import project view file**.

   <img width="991" alt="Screenshot 2025-03-19 at 18 44 41" src="https://github.com/user-attachments/assets/2679a750-414b-4680-8558-7f438e11f633" />

Click on the 3 dots to open the file picker. Scroll down and select `oppia-android.bazelproject`, then click **next**.

   <img width="991" alt="Screenshot 2025-03-19 at 18 45 23" src="https://github.com/user-attachments/assets/31e0694c-a179-4adf-8d4d-28793c586ee5" />

6. The project view will be generated as follows. Click the **Create** button to complete the import.

   ![Screenshot 2025-03-19 at 18 47 57](https://github.com/user-attachments/assets/b7016ab8-9ae5-44e0-a624-c7cc8fc576da)

7. Immediately, the project will begin to synchronize, and will be ready once sync completes. Read more about syncing in the [Bazel User Guide](https://github.com/oppia/oppia-android/wiki/Bazel-User-Guide#Syncing-the-project).

   ![Screenshot 2025-03-19 at 18 49 59](https://github.com/user-attachments/assets/603d85d6-1336-4893-8153-eef8caef5e81)

**Note**: Unlike Gradle, Bazel does not support the "**Android**" project view. To see all project directories, switch to the "**Project**" view instead.

In the `.aswb` directory, you will find the generated `.bazelproject` file. It should contain:

```shell
import oppia-android.bazelproject
```

8. Once sync has finished, you can now build and install the app on either a virtual or physical device. Bazel supports deploying to only one device at a time, so you can connect one device, or launch one emulator at a time.

   You can run the project by using the Bazel plugin to set up run configurations for the target that you wish to build. This performs the same action as the run commands listed in the [Bazel user guide](https://github.com/oppia/oppia-android/wiki/Bazel-User-Guide), but using the GUI to run the app might be more intuitive for some developers.

   ![Screenshot 2025-02-26 at 22 16 32](https://github.com/user-attachments/assets/f3be5288-dc96-4079-bcb2-b514de81f899)

   Edit configurations allows us to specify the run command:

   ![Image](https://github.com/user-attachments/assets/7270b6b3-11ac-4b49-a39a-734e0437bb9e)

     - Target **Name**, is helpful for identifying the target from the list on the left.
     - **Target expression** requires a build target such as `//:oppia_dev_binary`.
     - **The Bazel command** is `mobile-install`
     - **Bazel flags** are optional.
     - Select **Apply** and then **Close** or **Ok**.

## Set up and run tests
Testing the app is an integral part of our development process. You will need to test all code changes to ensure that the app works correctly, therefore it is important to ensure that your test configuration works.

Our Bazel setup currently supports running tests on Robolectric which is fast because it does not require a physical device or emulator setup.

We can run tests either through the UI supported by the Bazel Plugin, or via running a `bazel test` command on the terminal.

Please refer to the [Bazel User Guide](https://github.com/oppia/oppia-android/wiki/Bazel-User-Guide) for more information on how to run tests via the terminal.

### Running app module tests

1. In Android Studio, open the test file that you wish to run. If sync completed successfully when the project was opened, there will be a green run arrow next to the class name, as well as next to each individual test name. Clicking on the arrow will run either the full class or a single test as selected.

2. Because we have set up the Bazel plugin, we can also run a test class using the run arrow at the top of the project window:

   ![Screenshot 2025-02-26 at 22 16 32](https://github.com/user-attachments/assets/f3be5288-dc96-4079-bcb2-b514de81f899)

   Edit configurations allows us to specify the run command:
   ![Screenshot 2025-02-26 at 22 19 34](https://github.com/user-attachments/assets/462d07d2-4407-4898-bbc4-544509bd7486)

   - Test **name** is helpful for identifying the test target from the list on the left.
   - **Target expression** requires the fully qualified path to the test file.
   - **The Bazel command** for running tests is `test`
   - **Bazel flags** are optional.
   - Select **Apply** and then **Close** or **Ok**.

### Running non-app module tests

These are tests in other modules, such as **domain** or **utility**.

1. In Android Studio, open the test file that you wish to run. If sync completed successfully when the project was opened, there will be a green run arrow next to the class name, as well as next to each individual test name. Clicking on the arrow will run either the full class or a single test as selected.
   
   ![Screenshot 2025-03-18 at 21 35 11](https://github.com/user-attachments/assets/4f83c8d2-cbb9-49cb-ac3b-3e500e1f82f6)

2. A second way to run a test class would be to open the `BUILD` file located in the same package as the test file, and clicking on the green arrow next to the test target name.
  
   ![Image](https://github.com/user-attachments/assets/c1ee9e23-b0b2-430a-9592-9f87a2fdcbf0)

### Next Steps
- Congratulations, you are ready to work on your first issue! Take a look at our [good first issues](https://github.com/oppia/oppia-android/issues?q=is%3Aopen+is%3Aissue+label%3A%22good+first+issue%22+no%3Aassignee) and leave a comment with your suggested fix. A maintainer will assign you the issue and provide any necessary guidance.

- When you are ready to submit a PR, please follow [these instructions](https://github.com/oppia/oppia-android/wiki/Guidance-on-submitting-a-PR) on submitting a PR.

- To learn about how we write tests at oppia-android, please refer to: [Oppia Android Testing](https://github.com/oppia/oppia-android/wiki/Oppia-Android-Testing).

- An important reference to go back to as you continue developing with Bazel is the [Bazel User Guide](https://github.com/oppia/oppia-android/wiki/Bazel-User-Guide).

### Troubleshooting Installation
#### General issues
1. If the Static Checks on your PR keep failing, please refer to [instructions on running static checks locally](https://github.com/oppia/oppia-android/wiki/Static-Analysis-Checks#how-to-run-static-checks-locally) in order to catch these errors before pushing.
2. If you are using Android Studio or another UI-based git to push, you might see unclear errors such as `Error: Failed to push some refs to git@github.com:<your_user_name>/oppia-android.git`. We recommend using the command line to run all git commands.
3. **Error**: Unresolved reference `DaggerXXTest_TestApplicationComponent`. **Solution**: Don't worry this is not an error. Just run the test file and it will solve the error. For running tests, you can see [Oppia Android Testing](https://github.com/oppia/oppia-android/wiki/Oppia-Android-Testing) document.
4. Crashing layout tags in tablet, e.g.:
   **Error**: `java.lang.IllegalArgumentException: The tag for topic_lessons_title is invalid. Received: layout-sw600dp-port/topic_lessons_title_0`
   **Solution**: This error occurs when we remove any xml file which is related to tablet devices
   To solve this
      - Uninstall the app from tablet
      - Rebuild the app.
      - Run the app again.

#### Bazel issues
The team is in the process of migrating away from Gradle to Bazel, so while the project still has Gradle build files, they are no longer maintained and cannot be used to build the project.

Please note that:
- The IntelliJ Bazel plugin currently has some known issues:
    - Significant memory overhead that continues to grow without careful pruning (i.e. periodic shutdowns of the local Bazel build server). On some Linux distros or MacOS, this can result in a Kernel panic when memory is fully exhausted.
    - Various symbolic errors throughout the codebase that can make it much more difficult to jump to specific symbols (though, unlike Gradle, all code including scripts are editable and runnable within Android Studio).
    - Syntax highlighting errors in some instances.
- As the team finishes the migration to Bazel, communications and instructions will be sent ahead of time, and we will continually update our documentation with the latest changes.

Other problems that you may run into:

1. No matching toolchains (sdk_toolchain_type)
    ```
    ERROR: While resolving toolchains for target //:oppia_dev: no matching toolchains found for types
    @bazel_tools//tools/android:sdk_toolchain_type
    ERROR: Analysis of target '//:oppia_dev' failed; build aborted: no matching toolchains found for types
    @bazel_tools//tools/android:sdk_toolchain_type
    INFO: Elapsed time: 12.805s
    INFO: 0 processes.
    FAILED: Build did NOT complete successfully (13 packages loaded, 51 targets configured)
    ```
   Follow the [steps](https://docs.bazel.build/versions/main/tutorial/android-app.html#integrate-with-the-android-sdk) to add the ANDROID_HOME environment variable.

2. java.lang.ClassNotFoundException: com.android.tools.r8.compatdx.CompatDx
   If, when building the app binary, you encounter a failure that indicates that the `CompatDx` file cannot be found, this is likely due to you using a newer version of the Android build tools. You can manually downgrade to an older version of build-tools (particularly 32.0.0). Unfortunately, this can't be done through Android Studio but it can be done over a terminal. Follow the instructions listed [here](https://github.com/oppia/oppia-android/issues/3024#issuecomment-884513455) to downgrade your build tools & then try to build the app again.

3. If you encounter this error while building bazel on Apple Silicone Mac:
      ```
      ERROR: /Users/OpenSource/oppia-android/model/src/main/proto/BUILD.bazel:167:20: Generating JavaLite proto_library //model/src/main/proto:profile_proto failed: (Segmentation fault): protoc failed: error executing command bazel-out/darwin-opt-exec-2B5CBBC6/bin/external/com_google_protobuf/protoc '--proto_path=bazel-out/android-armeabi-v7a-fastbuild/bin/model/src/main/proto/_virtual_imports/languages_proto' ... (remaining 8 argument(s) skipped)

      Use --sandbox_debug to see verbose messages from the sandbox protoc failed: error executing command bazel-out/darwin-opt-exec-2B5CBBC6/bin/external/com_google_protobuf/protoc '--proto_path=bazel-out/android-armeabi-v7a-fastbuild/bin/model/src/main/proto/_virtual_imports/languages_proto' ... (remaining 8 argument(s) skipped)
      ```
    Bazel requires Xcode commandline tools to build on M1, and the Xcode license also needs to be accepted.
    
    **Follow these steps to solve this error:**
    
    - Install the commandline tools: `xcode-select --install`
    
    - Accept the Xcode licence: `sudo xcodebuild -licence`
    
    - Reset the xcode select path: `sudo xcode-select -r `
    
    - Set the xcode select path to use CommandLineTools: `sudo xcode-select -s /Library/Developer/CommandLineTools`
        Note: As of macOS 15.2.1(Sequoia), the full Xcode is required rather than just the CLT. The xcode-select path needs to point to Xcode instead:
        `sudo xcode-select -s /Applications/Xcode.app/Contents/Developer`
    
    - Confirm that the path was correctly set. The expected output is: `/Library/Developer/CommandLineTools` or `/Applications/Xcode.app/Contents/Developer` in macOS 15 and later.
    
        `xcode-select -p`
    
    After successfully running the above commands, build the app using Bazel by running the following command:

      ```
      bazel clean --expunge
      bazel build //:oppia_dev
      ```
4. If you run into a failure like the following when trying to use `mobile-install` to a device running SDK 30 or newer:

    ```
    FATAL EXCEPTION: main
    Process: org.oppia.android, PID: 9508
    java.lang.RuntimeException: Unable to instantiate application com.google.devtools.build.android.incrementaldeployment.StubApplication package org.oppia.android: java.lang.SecurityException: Writable dex file '/data/local/tmp/incrementaldeployment/org.oppia.android/dex/incremental_classes4.dex' is not allowed.
    at android.app.LoadedApk.makeApplicationInner(LoadedApk.java:1466)
    at android.app.LoadedApk.makeApplicationInner(LoadedApk.java:1395)
    at android.app.ActivityThread.handleBindApplication(ActivityThread.java:6959)
    at android.app.ActivityThread.-$$Nest$mhandleBindApplication(Unknown Source:0)
    at android.app.ActivityThread$H.handleMessage(ActivityThread.java:2236)
    at android.os.Handler.dispatchMessage(Handler.java:106)
    at android.os.Looper.loopOnce(Looper.java:205)
    at android.os.Looper.loop(Looper.java:294)
    at android.app.ActivityThread.main(ActivityThread.java:8177)
    at java.lang.reflect.Method.invoke(Native Method)
    at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:552)
    at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:971)
    Caused by: java.lang.SecurityException: Writable dex file '/data/local/tmp/incrementaldeployment/org.oppia.android/dex/incremental_classes4.dex' is not allowed.
    ```
    
    Then you will need to use `adb install` directly:
    
    ```shell
    adb install bazel-bin/oppia_dev_binary.apk
    ```

### Can’t find a particular issue?

If the error you get is not in the Troubleshooting section above, please post a request for help on the team's discussions board for installation problems: https://github.com/oppia/oppia-android/discussions/categories/q-a-installation.