## Instructions

**The steps to install Bazel on Linux are:**
1. Install Bazel
2. Set up the ANDROID_HOME environment variable
3. Verify the Android build

### 1. Install Bazel

Install Bazel from [here](https://docs.bazel.build/versions/master/install.html). Make sure that you follow the instructions for installing a specific version (Oppia Android requires 6.5.0 and won't build on other versions).
   - Note: if you find any errors related to `cURL`, please set up cURL on your machine. For Linux, you can use `sudo apt install curl`.

### 2. Set up the ANDROID_HOME environment variable

Ensure that your `ANDROID_HOME` environment variable is set to the location of your Android SDK. To do this, find the path to the installed SDK using Android Studio’s SDK Manager (install SDK 28). Assuming the SDK is installed to default locations, you can use the following commands to set the `ANDROID_HOME` variable:<br>
   ```
   export ANDROID_HOME=$HOME/Android/Sdk/
   ```

**Make sure you have the system environment variable set up** for ``ANDROID_HOME`` as you might have issues getting properly set up if not. If it isn’t set up (on Linux you can check by using ``echo $ANDROID_HOME`` in a new terminal; it should output the correct path to your Android SDK), on Linux you can move the ``export`` from above to your ``~/.bashrc`` file to make it permanent (you can apply the change immediately using ``source ~/.bashrc``).

### 3. Verifying the build

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
