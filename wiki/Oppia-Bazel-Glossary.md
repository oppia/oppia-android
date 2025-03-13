## Table of Contents

- [Overview](#overview)
- [Installation](#installation)
  - [Building the app](#building-the-app)
  - [Running specific module (app) Robolectric tests](#running-specific-module-app-robolectric-tests)
  - [Running all Robolectric tests (slow)](#running-all-robolectric-tests-slow)
- [Concepts and Terminology](#concepts-and-terminology)
- [Syncing the Project](#syncing-the-project)

## Overview
Bazel is an open-source build and test tool similar to Make, Maven, and Gradle. It uses a human-readable, high-level build language.

**WARNING: We recommend to not use the Android Studio Bazel plugin since it currently has compatibility issues with the project.**

## Installation

Follow the instructions on the [installation page](https://github.com/oppia/oppia-android/wiki/Installing-Oppia-Android#install-bazel) to set up Bazel.

### Building the app

After the installation completes you can build the app using Bazel.

**Move your command line head to the `~/opensource/oppia-android`**, then run the below bazel command:

On Sdk 29 and below, run:
```
bazel mobile-install //:oppia_dev_binary
```
This will build, install and launch the app on your device.

On Sdk 30 and newer, run:
```
bazel build //:oppia_dev
```
followed by:
```
adb install bazel-bin/oppia_dev_binary.apk  
```

### Running specific module (app) Robolectric tests

```
bazel test //app/...
```

### Running all Robolectric tests (slow)

```
bazel test //...
```

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
when we want to run test cases on Bazel build environment, a test target needs to be set up correctly:

```bazel
load("//:oppia_android_test.bzl", "oppia_android_test")

oppia_android_test(
    name = "MathExpressionInteractionsViewTest",
    srcs = [
        "MathExpressionInteractionsViewTest.kt",
        "//app:data_binder_mapper_impl"
    ],
    custom_package = "org.oppia.android.app.test",
    test_manifest = "//app:test_manifest",
    enable_data_binding = True,
    test_class = "org.oppia.android.app.customview.interaction.MathExpressionInteractionsViewTest",
    deps = [
      ...
    ],
)
```

The above assumes that the corresponding test requires resources. If it doesn't, the definition can be a bit simpler:

```bazel
load("//:oppia_android_test.bzl", "oppia_android_test")

oppia_android_test(
    name = "MathExpressionAccessibilityUtilTest",
    srcs = ["MathExpressionAccessibilityUtilTest.kt"],
    custom_package = "org.oppia.android.app.utility.math",
    test_class = "org.oppia.android.app.utility.math.MathExpressionAccessibilityUtilTest",
    test_manifest = "//app:test_manifest",
    deps = [
      ...
    ],
)
```

## Syncing the Project
The IntelliJ Bazel Plugin's Sync process has a purpose to query Bazel for information and build up IntelliJ's project structure to fit Bazel's model.

It runs automatically during a project import, and manually by either clicking on the sync icon in the menu bar or, partially syncing packages and individual files in contextual menus.

Running a sync generates a `.aswb` directory in the project root. 

### Structure of the .aswb
The `.aswb` is known as the **project directory**. It contains metadata about the project that bridges Bazel and IntelliJ project models.
```
.aswb/
├── .bazelproject
├── .blaze
│   ├── aar_libraries
│   ├── modules
│   ├── remoteOutputCache 
│   └── renderjars
└── .idea
```

The `.bazelproject` is the project view file which contains project-wide settings, like targets to sync, Bazel flags, and enabled languages. It is used to import a subset of Bazel packages into the IDE. The project view determines which rules are imported and how. Read more information [here](https://ij.bazel.build/docs/project-views.html).

The `.blaze` is the Bazel data subdirectory, containing mostly IntelliJ module definitions. 
  - `modules` directory contains IntelliJ module definition files.
  - `remoteOutputCache` is a general-purpose local cache for output artifacts generated remotely. During a project sync, updated outputs of interest will be copied locally.
  - `aar_libraries` is the location of the plugin's JAR cache. This helps provide a more robust code navigation experience, but with the possibility of missing changes made by Bazel outside of the IDE view.

The `.idea` directory contains project-specific settings files managed by IntelliJ. IntelliJ reads XML files in this directory to set up the Project Structure: project, modules, libraries, SDKs, facets.

### Syncing (and Partial Syncing) the Bazel Plugin in Android Studio
When you make certain changes to your project files, you need to sync your project with Bazel to pick up those changes. For example:
* Changing a BUILD file, like adding a new target, or adding dependencies and sources to a target.
* Changing generated output files required to resolve source code, like annotation processor outputs such as AutoValue-generated classes.

You can sync with Bazel in two ways:
* From your IDE’s menu, click **Bazel > Sync > Sync Project with BUILD files.**
* From the toolbar, click the **Sync Project with BUILD files** button.

**Expand Sync to Working Set**
Your working set is any files your VCS says are dirty, roughly corresponding to something like git status. By default the plugin tries to expand the sync to cover any target in your working set. This ensures these files are refreshed without having to go to the trouble of adding a temporary target to your project view.
- This sometimes causes problems, and you may see a warning in your Bazel sync tool window to disable it.

**Non-Incrementally Sync Project with BUILD Files**
This option recomputes certain things that are otherwise cached. 
- You should never have to use this option, but exists for debugging/fallback purposes.

**Sync Working Set**
Your working set is any files your VCS says are dirty, roughly corresponding to something like git status. This option tries to sync only your working set, cutting down on sync time, which is useful if you want to quickly bring in new dependencies in some files you are working on.

**Partially Sync File with Bazel**
Syncs only the targets corresponding to the file from which this action is invoked. Can cut down on the time to sync the project if you’re only interested in resolving a single file.
- This is invoked from the current active file.

**Automatic Sync**
You can enable automatic syncing in **Settings > Other Settings > Bazel > Auto sync on**. Automatic syncing occurs whenever BUILD files change. For many projects this is too slow, so this option is disabled by default.