## Bazel Set up for Mac including Apple silicon (M1/M2) chips

## Instructions

**The steps to install Bazel on Mac are:**
1. Set up Rosetta Terminal
2. Install Bazel
3. Install OpenJDK 11
4. Install Python 2 and make sure it is active in your environment
5. Set Bazel, Python 2, ANDROID_HOME paths permanently in your terminal
6. Prepare the build environment
7. Verify that the build is working

### 1. Set up Rosetta Terminal

- In the Finder app on your Mac, locate the Applications folder from the favorites sidebar.
- Right-click on your Terminal app and create a duplicate Terminal (and rename it accordingly, say **Terminal Rosetta**, to avoid confusion).
- On the newly created Terminal Rosetta icon, right-click and select "Get info", and under “General”, check the option "Open using Rosetta".

**Note: Always use the Rosetta terminal for Bazel setup and running `bash setup.sh` or any Bazel build-related commands.**

### 2. Install Bazel

1. Install Bazel following the instructions [here](https://docs.bazel.build/versions/4.0.0/install-os-x.html#install-with-installer-mac-os-x). Make sure that you follow the instructions for installing a specific version (Oppia Android requires 6.5.0 and won't build on other versions).

2. That’s it, now Bazel is installed, and you can verify it by running the command:
   ```
   bazel --version
   ```
   - **Expected Output**
   ```
   bazel 6.5.0
   ```

### 3. Install OpenJDK 11

Oppia Android also requires OpenJDK 11.
Follow the instructions [here](https://formulae.brew.sh/formula/openjdk@11) to install OpenJDK 11.
Note that this requires the installation of brew as a pre-requisite, which can be done by following the instructions [here](https://mac.install.guide/homebrew/index.html). You can then set up your `$JAVA_HOME` environment variable using these [instructions](https://stackoverflow.com/a/75167958/11396524).


### 4. Install Python 2 and make sure it is active in your environment

To install Python 2 in MacOS follow the follows the commands given below. Note that this requires installation of brew as a pre-requisite, which can be done by following the instructions [here](https://mac.install.guide/homebrew/index.html).
```
brew install pyenv
pyenv install 2.7.18
pyenv global 2.7.18
```

- To make sure Python 2 is successfully installed and active in your environment, navigate to the **oppia-android** directory and run the following commands:

```
export PATH="$(pyenv root)/shims:${PATH}"
python --version
```

### 5. Set Bazel, Python 2, ANDROID_HOME paths permanently in your terminal

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

**Note: You must set the path for `Bazel`, `Python 2`, `ANDROID_HOME` before running bazel build for oppia-android, otherwise you will get an error.**

### 6. Verify that the build is working

At this point, your system should be able to build Oppia Android. To verify, try building the APK (from your subsystem terminal -- note that this and all other Bazel commands must be run from the root of the ‘oppia-android’ directory otherwise they will fail):

```
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

Note also that the ``oppia.apk`` under the ``bazel-bin`` directory of your local copy of Oppia Android should be a fully functioning development version of the app that can be installed using ``adb``
