## Bazel Setup for Mac including Apple silicon (M1/M2) chips

## Instructions

**The steps to install Bazel on Mac are:**
1. Setup Rosetta Terminal
2. Install Bazel 
3. Install OpenJDK 8
4. Install Python 2 and make sure it is active in your environment
5. Set up the ANDROID_HOME environment variable
6. Set Bazel, Python 2, ANDROID_HOME paths permanently in your terminal
7. Prepare the build environment
8. Verify that the build is working

### 1. Setup Rosetta Terminal

- In the Finder app on your Mac, locate the Applications folder from the favorites sidebar.
- Right-click on your Terminal app and create a duplicate Terminal (and rename it accordingly, say **Terminal Rosetta**, to avoid confusion).
- On the newly created Terminal Rosetta icon, right-click and select "Get info", and under “General”, check the option "Open using Rosetta".

**Note: Always use the Rosetta terminal for Bazel setup and running `bash setup.sh` or any Bazel build-related commands.**

### 2. Install Bazel

1. Install Bazel following the instructions [here](https://docs.bazel.build/versions/4.0.0/install-os-x.html#install-with-installer-mac-os-x). Make sure that you follow the instructions for installing a specific version (Oppia Android requires 4.0.0 and won't build on other versions). 

2. That’s it, now Bazel is setup permanently in your terminal, you can again check it by running the command:
   ```
   bazel --version
   ```
   - **Expected Output**
   ```
   bazel 4.0.0
   ```

**Note: You must set the path for `bazel 4.0.0` before running bazel build for oppia-android, otherwise you will get an error.**

### 3. Install OpenJDK 8

Oppia Android also requires OpenJDK 8.
Follow the instructions [here](https://installvirtual.com/install-openjdk-8-on-mac-using-brew-adoptopenjdk/) to install OpenJDK 8. 
Note that this requires the installation of brew as a pre-requisite, which can be done by following the instructions [here](https://mac.install.guide/homebrew/index.html). You can then set up your `$JAVA_HOME` environment variable using these [instructions](https://stackoverflow.com/a/75167958/11396524).


### 4. Installing Python 2

```
brew install pyenv
pyenv install 2.7.18
pyenv global 2.7.18
```

- To make sure Python 2 is active in your environment, navigate to the **oppia-android** directory and run the following commands before running the Bazel build.

```
export PATH="$(pyenv root)/shims:${PATH}"
python --version
```

**Note: You must set the path for `Python 2` before running bazel build for oppia-android, otherwise you will get an error.**

### 5. Set up the ANDROID_HOME environment variable

- Ensure that your `ANDROID_HOME` environment variable is set to the location of your Android SDK before running Bazel build.

- To do this, find the path to the installed SDK using Android Studio’s SDK Manager (install SDK 28). Assuming the SDK is installed to default locations, you can use the following command to set the `ANDROID_HOME` variable:
```
export ANDROID_HOME=$HOME/Library/Android/sdk
```
- To confirm that it is set, run the following command:
```
echo $ANDROID_HOME
```

**Note: You must set the path for `ANDROID_HOME` before running bazel build for oppia-android, otherwise you will get an error.**

### 6. Set Bazel, Python 2, ANDROID_HOME paths permanently in your terminal

- To set the `Bazel`, `Python 2`, `ANDROID_HOME` path permanently in your terminal run these commands:
    ```
    sudo nano /etc/paths
    ```
   - Enter your password, when prompted.
   - Go to the bottom of the file, and enter these paths 
     ```
     /Users/{YourMacUserName}/bin
     $(pyenv root)/shims:${PATH}
     $HOME/Library/Android/sdk
     ```
   - Hit control-x to quit.
   - Enter “Y” to save the modified buffer.
   - That’s it!  To test it, in a new terminal window, type: `echo $PATH`

### 7. Preparing the build environment for Oppia-Android

Follow the instructions in [oppia-bazel-tools](https://github.com/oppia/oppia-bazel-tools#readme), in order to prepare your environment to support Oppia Android builds.

### 8. Verifying the build

At this point, your system should be able to build Oppia Android. To verify, try building the APK (from your subsystem terminal -- note that this and all other Bazel commands must be run from the root of the ‘oppia-android’ directory otherwise they will fail):

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
