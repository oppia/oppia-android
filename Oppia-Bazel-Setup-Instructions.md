## Overview
Bazel is an open-source build and test tool similar to Make, Maven, and Gradle. It uses a human-readable, high-level build language.

## Installation

**WARNING: We recommend to not use the Android Studio Bazel plugin since it currently has compatibility issues with the project.**

**NOTE: If you're using Windows, please follow [these instructions](https://github.com/oppia/oppia-android/wiki/Bazel-Setup-Instructions-for-Windows) instead.**

Instructions for setting up Bazel on Unix-based machines:

1. Install Bazel from [here](https://docs.bazel.build/versions/master/install.html). Make sure that you follow the instructions for installing a specific version (Oppia Android requires 4.0.0 and won't build on other versions).

2. Oppia Android also requires OpenJDK 8. The Bazel installation instructions above include [sections on installing OpenJDK](https://docs.bazel.build/versions/main/tutorial/java.html#install-the-jdk) on different platforms. 

   - For example, if you're using Ubuntu or another Debian-based system, you can run the following to install OpenJDK 8:

     ```sh
     sudo apt install openjdk-8-jdk
     ```

   - For MacOS M1, follow the instructions [here](https://installvirtual.com/install-openjdk-8-on-mac-using-brew-adoptopenjdk/). Note that, this requires installation of brew as a pre-requisite, which can be done by following the instructions [here](https://mac.install.guide/homebrew/index.html).
   
    You can confirm that this is set up using the command `java -version`, which should result in three lines being printed out with the first one showing "openjdk version "1.8.0_292".

3. Ensure that your `ANDROID_HOME` environment variable is set to the location of your Android SDK. To do this, find the path to the installed SDK using Android Studio’s SDK Manager (install SDK 28). Assuming the SDK is installed to default locations, you can use the following commands to set the `ANDROID_HOME` variable:<br>
    - Linux: `export ANDROID_HOME=$HOME/Android/Sdk/`<br>
    - macOS: `export ANDROID_HOME=$HOME/Library/Android/sdk`

4. Follow the instructions in [oppia-bazel-tools](https://github.com/oppia/oppia-bazel-tools).

#### Possible Error:
```
ERROR: While parsing option --override_repository=android_tools=~/oppia-bazel/android_tools: Repository 
override directory must be an absolute path
```
Try to delete the `.bazelrc` file to solve the above. error. 

After the installation completes you can build the app using Bazel. 

**Move your command line head to the `~/opensource/oppia-android`**, then run the below bazel command:

#### Building the app

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

## Known issues

#### java.lang.ClassNotFoundException: com.android.tools.r8.compatdx.CompatDx

If, when building the app binary, you encounter a failure that indicates that the CompatDx file cannot be found, this is likely due to you using a newer version of the Android build tools. You can manually downgrade to an older version of build-tools (particularly 29.0.2). Unfortunately, this can't be done through Android Studio but it can be done over a terminal. Follow the instructions listed [here](https://github.com/oppia/oppia-android/issues/3024#issuecomment-884513455) to downgrade your build tools & then try to build the app again.

## Concepts and Terminology
**[Workspace](https://github.com/oppia/oppia-android/blob/develop/WORKSPACE)**<br>
A workspace is a directory where we add targetted SDK version, all the required dependencies and there required Rules. The directory containing the WORKSPACE file is the root of the main repository, which in our case is the `oppia-android` root directory is the main directory. 

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
load("@io_bazel_rules_kotlin//kotlin:kotlin.bzl", "kt_android_library")
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

