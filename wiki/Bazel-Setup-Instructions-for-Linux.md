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
bazel run //:install_oppia_dev
```
