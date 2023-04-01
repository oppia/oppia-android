## Overview
Bazel is an open-source build and test tool similar to Make, Maven, and Gradle. It uses a human-readable, high-level build language.

## Installation

**WARNING: We recommend to not use the Android Studio Bazel plugin since it currently has compatibility issues with the project.**

**NOTE: If you're using Windows, please follow [these instructions](https://github.com/oppia/oppia-android/wiki/Bazel-Setup-Instructions-for-Windows) instead.**

Instructions for setting up Bazel on Unix-based machines:

1. Install Bazel from [here](https://docs.bazel.build/versions/master/install.html). Make sure that you follow the instructions for installing a specific version (Oppia Android requires 4.0.0 and won't build on other versions).
    - As of February 2023 we have verified that on Ubuntu (and similar systems) the [apt repository approach](https://bazel.build/install/ubuntu#install-on-ubuntu) works, you just need to make sure to do `sudo apt install bazel-4.0.0` as the latest command to install the correct version.
    - Note: if you find any errors related to `cURL`, please set up cURL on your machine. For Linux, you can use `sudo apt install curl`.


2. Oppia Android also requires OpenJDK 8. The Bazel installation instructions above include [sections on installing OpenJDK](https://docs.bazel.build/versions/main/tutorial/java.html#install-the-jdk) on different platforms. 

   - For example, if you're using Ubuntu or another Debian-based system, you can run the following to install OpenJDK 8:

     ```sh
     sudo apt install openjdk-8-jdk
     ```

   - For MacOS M1, follow the instructions [here](https://installvirtual.com/install-openjdk-8-on-mac-using-brew-adoptopenjdk/). Note that, this requires installation of brew as a pre-requisite, which can be done by following the instructions [here](https://mac.install.guide/homebrew/index.html).
   
    You can confirm that this is set up using the command `java -version`, which should result in three lines being printed out with the first one showing "openjdk version "1.8.0_292".

3. Ensure that you have Python 2 installed and make sure that it is currently active on your environment. You can do this by using the ``python --version`` command which should show Python 2.X.X. If it doesn’t, click [here](https://linuxconfig.org/install-python-2-on-ubuntu-20-04-focal-fossa-linux) for a resource on how to install and update Ubuntu to use Python 2 (other distros may vary in this step slightly).

4. Ensure that your `ANDROID_HOME` environment variable is set to the location of your Android SDK. To do this, find the path to the installed SDK using Android Studio’s SDK Manager (install SDK 28). Assuming the SDK is installed to default locations, you can use the following commands to set the `ANDROID_HOME` variable:<br>
    - Linux: `export ANDROID_HOME=$HOME/Android/Sdk/`<br>
    - macOS: `export ANDROID_HOME=$HOME/Library/Android/sdk`
    - **Make sure you have the system environment variable set up** for ``ANDROID_HOME`` as you might have issues getting properly set up if not. If it isn’t set up (on Linux you can check by using ``echo $ANDROID_HOME`` in a new terminal; it should output the correct path to your Android SDK), on Linux you can move the ``export`` from above to your ``~/.bashrc`` file to make it permanent (you can apply the change immediately using ``source ~/.bashrc``).

5. Follow the instructions in [oppia-bazel-tools](https://github.com/oppia/oppia-bazel-tools).


#### Building the app

After the installation, completes you can build the app using Bazel. 

**Move your command line head to the `~/opensource/oppia-android`**, then run the below bazel command:

```
bazel build //:oppia
```

#### Building + installing the app

```
bazel build //:oppia && adb install -r bazel-bin/oppia.apk
```

#### Running specific module (app) Robolectric tests

```
bazel test //app/...
```

#### Running all Robolectric tests (slow)

```
bazel test //...
```

## Known Issues and Troubleshooting

See our [troubleshooting wiki page](https://github.com/oppia/oppia-android/wiki/Troubleshooting-Installation#bazel-issues) for some known issues with Bazel, and the corresponding troubleshooting steps.


## Concepts and Terminology
**[Workspace](https://github.com/oppia/oppia-android/blob/develop/WORKSPACE)**<br>
A workspace is a directory where we add targeted SDK version, all the required dependencies and there required Rules. The directory containing the WORKSPACE file is the root of the main repository, which in our case is the `oppia-android` root directory is the main directory. 

**[Packages](https://github.com/oppia/oppia-android/tree/develop/app)**<br>
A package is defined as a directory containing a file named BUILD or BUILD.bazel.

**[Binary rules](https://github.com/oppia/oppia-android/blob/ba8d914480251e4a8543feb63a93b6c91e0a5a2f/BUILD.bazel#L3)**<br>
A rule specifies the relationship between inputs and outputs, and the steps to build the outputs.
In Android, rules are defined using `android_binary`. Android rules for testing are `android_instrumentation_test` and `android_local_test`.

**[BUILD files](https://github.com/oppia/oppia-android/blob/develop/app/BUILD.bazel)**<br>
Every package contains a BUILD file. This file is written in Starlark Language. In this Build file for module-level, we generally define `android_library`, `kt_android_library` to build our package files as per the requirement. 

**[Dependencies](https://github.com/oppia/oppia-android/blob/ba8d914480251e4a8543feb63a93b6c91e0a5a2f/BUILD.bazel#L16)**<br>
A target A depends upon a target B if B is needed by A at build. `A -> B`<br>
```
deps = [ "//app",]
```
Here, `deps` is used to define the dependencies which is a type of dependencies called `deps dependencies` and it includes the files/directory/target which are dependent. From the above example the dependency is the `app` target which is defined in the [Build file of app package](https://github.com/oppia/oppia-android/blob/ba8d914480251e4a8543feb63a93b6c91e0a5a2f/app/BUILD.bazel#L616). 

Example of Dependencies
1. [srcs dependencies](https://github.com/oppia/oppia-android/blob/ba8d914480251e4a8543feb63a93b6c91e0a5a2f/app/BUILD.bazel#L617)
2. [deps dependencies](https://github.com/oppia/oppia-android/blob/ba8d914480251e4a8543feb63a93b6c91e0a5a2f/app/BUILD.bazel#L622)

**[Loading an extension](https://github.com/oppia/oppia-android/blob/ba8d914480251e4a8543feb63a93b6c91e0a5a2f/app/BUILD.bazel#L13)**<br>
Bazel extensions are files ending in .bzl. Use the load statement to import a symbol from an extension.<br>
```
load("@io_bazel_rules_kotlin//kotlin:android.bzl", "kt_android_library")
```
Here, we are loading `kotlin.bzl` and we are going to use it with a symbol name `kt_android_library`.
Arguments to the load function must be string literals. load statements must appear at top-level in the file.

**[Visibility of a file target](https://github.com/oppia/oppia-android/blob/ba8d914480251e4a8543feb63a93b6c91e0a5a2f/app/BUILD.bazel#L621)**<br>
With the example from our codebase, target `app` whose visibility is public. <br>
 - `visibility = ["//visibility:public"],` - Anyone can use this target.<br>
 - `"//visibility:private"` - Only targets in this package can use this target.

**[Testing](https://github.com/oppia/oppia-android/blob/ba8d914480251e4a8543feb63a93b6c91e0a5a2f/app/BUILD.bazel#L719)**<br>
when we want to run test cases on Bazel build environment, we usually pass arguments related to test which `app_test.bazl` required to run our test.
```
app_test(
    name = "HomeActivityLocalTest",
    srcs = ["src/test/java/org/oppia/android/app/home/HomeActivityLocalTest.kt"],
    test_class = "org.oppia.android.app.home.HomeActivityLocalTest",
    deps = TEST_DEPS,
)
```

