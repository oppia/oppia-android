## Bazel Set up for Mac including Apple silicon (M1/M2) chips

## Instructions

**The steps to install Bazel on Mac are:**
1. Set up Rosetta Terminal
2. Install Bazel
3. Set Bazel and ANDROID_HOME paths permanently in your terminal
4. Verify that the build is working

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

### 3. Set Bazel and ANDROID_HOME paths permanently in your terminal

- To set the `Bazel` and `ANDROID_HOME` path permanently in your terminal run these commands:
    ```
    sudo nano /etc/paths
    ```
   - Enter your password, when prompted.
   - Go to the bottom of the file, and enter these paths
     ```
     /Users/{YourMacUserName}/bin
     $HOME/Library/Android/sdk
     ```
   - Hit control-x to quit.
   - Enter “Y” to save the modified buffer.
   - That’s it!  To test it, in a new terminal window, type: `echo $PATH`

**Note: You must set the path for `Bazel` and `ANDROID_HOME` before running bazel build for oppia-android, otherwise you will get an error.**

### 4. Verify that the build is working

At this point, your system should be able to build Oppia Android. To verify, try building the APK (from your subsystem terminal -- note that this and all other Bazel commands must be run from the root of the ‘oppia-android’ directory otherwise they will fail):

```
bazel build //:oppia_dev
```

(Note that this command may take 10-20 minutes to complete depending on the performance of your machine).

If everything is working, you should see output like the following:

```
Target //:oppia_dev up-to-date:
  bazel-bin/oppia_dev.aab
INFO: Elapsed time: ...
INFO: 1 process...
INFO: Build completed successfully, ...
```

Note also that the ``oppia_dev.aab`` under the ``bazel-bin`` directory of your local copy of Oppia Android should be a fully functioning development version of the app that can be installed using bundle-tool. However, it's recommended to deploy Oppia to an emulator or connected device using the following Bazel command:

```sh
bazel mobile-install //:oppia_dev_binary
```

``mobile-install`` is much faster for local development (especially for the developer flavor of the app) because it does more sophisticated dex regeneration detection for faster incremental installs. See https://bazel.build/docs/mobile-install for details.

**Note**: If you run into a failure like the following when trying to use `mobile-install` to a device running SDK 34 or newer:

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

```sh
adb install bazel-bin/oppia_dev_binary.apk
```
