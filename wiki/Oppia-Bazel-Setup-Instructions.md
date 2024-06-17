## Table of Contents

- [Overview](#overview)
- [Installation](#installation)
  - [Building the app](#building-the-app)
  - [Building + installing the app](#building--installing-the-app)
  - [Running specific module (app) Robolectric tests](#running-specific-module-app-robolectric-tests)
  - [Running all Robolectric tests (slow)](#running-all-robolectric-tests-slow)
- [Known Issues and Troubleshooting](#known-issues-and-troubleshooting)
- [Concepts and Terminology](#concepts-and-terminology)

## Overview
Bazel is an open-source build and test tool similar to Make, Maven, and Gradle. It uses a human-readable, high-level build language.

**WARNING: We recommend to not use the Android Studio Bazel plugin since it currently has compatibility issues with the project.**

### Installation

1. Download and Install Java 11 using the links from the [Java website](https://www.java.com/en/download/).

2. **Select your Operating System for instructions on setting up Bazel:**

   - [For Windows/Ubuntu/Fedora](https://github.com/oppia/oppia-android/wiki/Bazel-Setup-Instructions-for-Windows)
   - [For Mac including M1/M2](https://github.com/oppia/oppia-android/wiki/Bazel-Setup-Instructions-for-Mac)
   - [For Linux](https://github.com/oppia/oppia-android/wiki/Bazel-Setup-Instructions-for-Linux)

### Building the app

After the installation completes you can build the app using Bazel.

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
Here, we are loading `android.bzl` and we are going to use it with a symbol name `kt_android_library`.
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

