## Instructions

**The steps to install Bazel on Linux are:**
1. Install Bazel
2. Install OpenJDK 11
3. Install Python 2 and make sure it is active in your environment
4. Set up the ANDROID_HOME environment variable
5. Prepare the build environment
6. Verify the Android build

### 1. Install Bazel

Install Bazel from [here](https://docs.bazel.build/versions/master/install.html). Make sure that you follow the instructions for installing a specific version (Oppia Android requires 6.5.0 and won't build on other versions).
   - Note: if you find any errors related to `cURL`, please set up cURL on your machine. For Linux, you can use `sudo apt install curl`.

### 2. Install OpenJDK 11

Oppia Android also requires OpenJDK 11. The Bazel installation instructions above include [sections on installing OpenJDK](https://docs.bazel.build/versions/main/tutorial/java.html#install-the-jdk) on different platforms.

   - You can run the following to install OpenJDK 11:

     ```sh
     sudo apt install openjdk-11-jdk
     ```

   You can confirm that this is set up using the command `java -version`, which should result in three lines being printed out with one of them including version "11.x".

### 3. Install Python 2

Ensure that you have Python 2 installed and make sure that it is currently active on your environment. You can do this by using the ``python --version`` command which should show Python 2.X.X. If it doesn’t, click [here](https://linuxconfig.org/install-python-2-on-ubuntu-20-04-focal-fossa-linux) for a resource on how to install and update Linux to use Python 2.

> **Tip:** Newer linux distributions may not have the python command and instead use python2 and python3. Bazel <5 requires `python` and that [cannot be configured](https://stackoverflow.com/questions/62802126/how-to-stop-bazel-from-relying-on-python2). Creating an alias won't solve the problem, you'll need a symlink. To create one, you can run the following:
>
> `sudo apt install python-is-python2`

### 4. Set up the ANDROID_HOME environment variable

Ensure that your `ANDROID_HOME` environment variable is set to the location of your Android SDK. To do this, find the path to the installed SDK using Android Studio’s SDK Manager (install SDK 28). Assuming the SDK is installed to default locations, you can use the following commands to set the `ANDROID_HOME` variable:<br>
   ```
   export ANDROID_HOME=$HOME/Android/Sdk/
   ```
   - **Make sure you have the system environment variable set up** for ``ANDROID_HOME`` as you might have issues getting properly set up if not. If it isn’t set up (on Linux you can check by using ``echo $ANDROID_HOME`` in a new terminal; it should output the correct path to your Android SDK), on Linux you can move the ``export`` from above to your ``~/.bashrc`` file to make it permanent (you can apply the change immediately using ``source ~/.bashrc``).

### 5. Verifying the build

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

Note also that the ``oppia.apk`` under the ``bazel-bin`` directory of your local copy of Oppia Android should be a fully functioning development version of the app that can be installed using ``adb``
