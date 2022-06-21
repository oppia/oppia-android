## Overview & Disclaimer

This page outlines one way to allow Bazel to be used in CLI form on Windows. Please note that **this support is currently experimental**. You may run into some problems--we suggest that you [file an issue](https://github.com/oppia/oppia-android/issues/new/choose) ior contact us at [gitter](https://gitter.im/oppia/oppia-android).

Unlike Unix-based systems where Bazel runs natively without issue, the current solution on Windows is to install an Ubuntu-based subsystem. Windows currently only supports a terminal experience in this subsystem (though there is a prerelease version of the software with GUI support) which means Android Studio will not be supported. You will need to continue using the Windows version of Android Studio and only use the Linux subsystem for building & running Robolectric or JUnit-based tests.

## Instructions

**Main prerequisites**:
- Windows 10: These instructions are geared towards users of Windows 10 (older versions will not be compatible). If you're in using an older version of Windows, please follow up with a comment on [this issue](https://github.com/oppia/oppia-android/issues/3371).
- At least 4GB of free local disk storage space (for Linux & needed dependencies), but more will probably help when building the app

Also, note that these instructions *replace* the standard Oppia Bazel [set-up instructions](https://github.com/oppia/oppia-android/wiki/Oppia-Bazel-Setup-Instructions) since different steps are required for Windows with an Ubuntu subsystem.

At a high-level, the steps to make Bazel work on Windows are:
1. Install the Ubuntu subsystem
2. Install prerequisite debian packages
3. Install the Android SDK
4. Install Bazel
5. Set up the environment to be able to build Oppia Android
6. Verify that the build is working
7. Run Bazel commands as needed during development

### 1. Install Ubuntu subsystem

Please follow Microsoft's [setup instructions](https://docs.microsoft.com/en-us/windows/wsl/install-win10) to set up the Linux subsystem on Windows 10. From there, you should install **Ubuntu** (the instructions below are based on the apt package manager on Ubuntu; other Linux distributions & package managers may work but they are untested by the team).

Once installed, open the Ubuntu terminal from the start menu.

### 2. Prerequisite debian packages

From within the Ubuntu terminal, start by ensuring all packages are up-to-date:

```sh
sudo apt update && sudo apt upgrade
```

After that, follow each of the subsections below as needed to install prerequisite dependencies:

**Java**

JDK 8 is required for the Android build tools, and we suggest installing OpenJDK:

```sh
sudo apt install openjdk-8-jdk-headless
```

**Python 2**

Unfortunately, some of the Bazel build actions in the Android pipeline require Python 2 to be installed:

```sh
sudo apt install python
```

### 3. Installing the Android SDK

We need to be able to run Linux-compatible Android utilities which requires installing a Linux version of the Android SDK. Since we can't install Android Studio in the subsystem, we need to do this via CLI commands. The steps below are extracted from [this article](https://proandroiddev.com/how-to-setup-android-sdk-without-android-studio-6d60d0f2812a).

First, prepare the environment for the SDK by creating the default directory to hold the SDK (from within Ubuntu terminal):

```sh
mkdir -p $HOME/Android/Sdk
```

Second, navigate to https://developer.android.com/studio#command-tools in a web browser (in Windows) and select to download the latest **Linux** command tools (even though you're using Windows, the Linux commandline tools are needed--the Windows version will not work with these instructions). Once downloaded, copy the zip file to the new SDK location (note that the ``/mnt/c/...`` path is based on ``C:\Users\<Name>\Downloads`` being the default download location--this may not be the case on your system) with your Windows username filled in for ``<Name>``:

```sh
cp /c/mnt/Users/<Name>/Downloads/commandlinetools*.zip $HOME/Android/Sdk
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
sdkmanager --install "platforms;android-28"
sdkmanager --install "build-tools;29.0.2"
```

When the commands above are finished running, the Android SDK should now be installed in your subsystem & be accessible to Bazel.

### 4. Installing Bazel

Follow [these instructions](https://docs.bazel.build/versions/main/install-ubuntu.html#install-on-ubuntu) to install Bazel using ``apt`` rather than Bazelisk (Bazelisk might work, but it's untested with these instructions). Note that Oppia requires Bazel 4.0.0, so you'll likely need to run the following command:

```sh
sudo apt install bazel-4.0.0
```

### 5. Preparing build environment for Oppia Android

The Oppia Android repository generally expects to live under an 'opensource' directory. While we recommend doing that in practice, we run into one complication when building the app on Windows: the repository itself lives under the native Windows filesystem & most of everything else needed to build lives under the Linux subsystem. To help simplify things, we prefer keeping just the repository on Windows and everything else on Linux, including the the Oppia Bazel toolchain. To prepare for this, we suggest making an 'opensource' directory in your Ubuntu subsystem:

```sh
mkdir $HOME/opensource
```

From there, follow [these instructions](https://github.com/oppia/oppia-bazel-tools#readme) in order to prepare your environment to support Oppia Android builds.

### 6. Verifying the build

At this point, your system should be able to build Oppia Android. To verify, try building the APK (from your subsystem terminal):

```sh
bazel build //:oppia
```

(Note that this command may take 10-20 minutes to complete depending on the performance of your machine).

If everything is working, you should see output like the following:

```
Target //:oppia up-to-date:
  bazel-bin/oppia_deploy.jar
  bazel-bin/oppia_unsigned/apk
  bazel-bin/oppia/apk
INFO: Elapsed time: ...
INFO: 1 process...
INFO: Build completed successfully, ...
```

If you see the above, you can proceed to use your subsystem for Bazel commands while developing Oppia. If you instead see an error, please [file an issue](https://github.com/oppia/oppia-android/issues/new/choose).

Note also that the ``oppia.apk`` under the ``bazel-bin`` directory of your local copy of Oppia Android should be a fully functioning development version of the app that can be installed using ``adb`` (though we recommend using ADB from within a Windows command prompt or shell since the Ubuntu subsystem may not have correct support to access devices or emulators connected to the native Windows machine).

### 7. Next steps

At this point, commands listed on the other [Bazel setup instructions](https://github.com/oppia/oppia-android/wiki/Oppia-Bazel-Setup-Instructions#building-the-app) page should now work locally. Keep in mind that your development will continue on Windows via Android Studio and that the subsystem is only needed to execute Bazel commands (Gradle should continue to work through Windows).

## Appendix

### Limitations
Known limitations with using an Ubuntu subsystem on Windows:
- Android Studio must run on native Windows: this is a current limitation. However, https://github.com/microsoft/wslg is a WIP project that may provide an alternative option which allows full development to take place within the subsystem.
- The subsystem is very slow: unfortunately, this is just a limitation with how the subsystem works on Windows. Until we fix the actual build pipeline to work natively, this is likely going to be a limitation that we have to live with. Note that installing an Ubuntu VM or dual-booting Ubuntu may lead to less issues & better performance than using a subsystem, but this hasn't yet been investigated or documented yet (see [#3437](https://github.com/oppia/oppia-android/issues/3437) for the WIP issue).
- ADB is limited within the subsystem and thus must be used from within a Windows CLI like Command Prompt, Powershell, or Git Bash (if it's installed) in order to deploy the Bazel-built test or APK binary to an emulator or real device
- Emulators likely cannot be launched from the subsystem (headless might be possible, but this hasn't been tested)
