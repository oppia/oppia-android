## Table of Contents

- [Overview](#overview)
- [Installation](#installation)
  - [Building the app](#building-the-app)
- [Running Tests](#running-tests)
  - [How to Obtain a Test Target](#how-to-obtain-a-test-target)
  - [Common Flags](#common-flags)
  - [Running multiple test targets](#running-multiple-test-targets)
- [Concepts and Terminology](#concepts-and-terminology)
- [Syncing the Project](#syncing-the-project)

## Overview
Bazel is an open-source build and test tool similar to Make, Maven, and Gradle. It uses a human-readable, high-level build language.

Oppia Android leverages Bazel for its efficient, scalable builds and reliable performance across various environments, making it well-suited for our large-scale project.

## Installation

Follow the instructions on the [installation page](https://github.com/oppia/oppia-android/wiki/Installing-Oppia-Android#install-bazel) to set up Bazel.

### Building the app

The [installation page](https://github.com/oppia/oppia-android/wiki/Installing-Oppia-Android#install-bazel) provides information on running the project in Android Studio.

You can also run the app from the command line using the commands detailed below.

Using the command line is helpful in instances when you need to build and run some targets outside of Android Studio.

Run the following commands in your terminal. All Bazel commands must be run from the root of the `oppia-android` directory otherwise they will fail.

Note that on the first run, these commands may take 10-20 minutes to complete depending on the performance of your machine. Subsequent runs will be much faster.

**On SDK 29 and below, run**:
   ```
   bazel mobile-install //:oppia_dev_binary
   ```
This will build, install and launch the app on your device.

**On SDK 30 and newer, run**:
   ```
   bazel build //:oppia_dev
   ```
followed by:
   ```
   adb install bazel-bin/oppia_dev_binary.apk  
   ```
* Starting from SDK 30, incremental builds, like those executed using `bazel mobile-install`, are no longer permitted, necessitating the use of two separate commands.

If everything is working, you should see output like the following:

```
Target //:oppia_dev_binary up-to-date:
  bazel-bin/oppia_dev_binary_deploy.jar
  bazel-bin/oppia_dev_binary_unsigned.apk
  bazel-bin/oppia_dev_binary.apk
INFO: Elapsed time: 37.155s, Critical Path: 17.24s
INFO: 99 processes: 2 internal, 23 darwin-sandbox, 74 worker.
INFO: Build completed successfully, 99 total actions
Performing Streamed Install
Success
```

Note also that the ``oppia_dev.aab`` under the ``bazel-bin`` directory of your local copy of Oppia Android should be a fully functioning development version of the app that can be installed using bundle-tool. However, it's recommended to deploy Oppia to an emulator or connected device using the `mobile-install` command.

## Running Tests

The bazel test command is used to build and run tests in a Bazel workspace. It ensures that the specified test targets are built and executed in a sandboxed environment for reproducibility.

**Syntax**

```shell
bazel test //path/to:target
```

### How to Obtain a Test Target
A test target in Bazel refers to a specific testable entity defined in a BUILD file. It represents a test rule, such as `java_test` or `android_test`, that Bazel can build and execute.

To ensure you always use the correct test target, follow one of these steps:
1. If the test file is open in Android Studio:
   * Right-click to open the context menu and select **Copy BUILD target string** as shown.
     
     ![Screenshot 2025-03-18 at 22 30 22](https://github.com/user-attachments/assets/286ebcb6-4f4e-4055-b6f0-4c070b039375)
   
   * Paste the target to the bazel test command, e.g. `bazel test //domain/src/test/java/org/oppia/android/domain/onboarding:AppStartupStateControllerTest`

2. If the test file is open in Android Studio, but **Copy BUILD target string** is not available in the context menu:
Sometimes, the **Copy BUILD target string** is not available in the context menu either due to sync issues or modularization issues. You can still copy the file path, and pass it to the test command.
   * In the AS sidebar, right click on the file, and select `copy path/reference`.
     or
   * Right-click on the file tab and then click `copy path/reference`.
   * Next, select `copy path from repository root`. 
   * Paste the copied path to the bazel test command in the terminal and remove the `.kt` extension, e.g. `bazel test domain/src/test/java/org/oppia/android/domain/onboarding/AppStartupStateControllerTest`.

With this syntax, Bazel implicitly converts the file path into a target if it matches a test rule in the BUILD file. If the file is not part of a test target in the BUILD file, this command may fail.
**When to use this**: If you are unsure about the exact Bazel target and want a quick way to run a test.

3. Querying for a target
Using the explicit Bazel target (// syntax) is more reliable compared to the file path syntax above. However, in cases such as running the test outside android studio, you may not know the target. In this case, you can use Bazel's `query` command to retrieve the correct test target:
   * Copy the relative path of the test file.
   * Run the following command in your terminal to get the test’s Bazel target: `bazel query relative-path-of-test-file`
   Example
   
   ```shell
   bazel query domain/src/test/java/org/oppia/android/domain/onboarding/AppStartupStateControllerTest.kt
   ```
   * The output will be a Bazel target that starts with //. Copy the target and remove the .kt extension.
   * Use this target in the bazel test command, e.g. `bazel test //domain/src/test/java/org/oppia/android/domain/onboarding:AppStartupStateControllerTest`.

### Common Flags

`--test_output=all` → Show full test output.
`--cache_test_results=no` → Force re-running tests even if cached.
`--test_filter=ClassName#methodName` → Run specific test cases.
`--runs_per_test=100` → Specifies number of times to run each test.

### Running multiple test targets
To run all the test targets in the app module:

```
bazel test //app/...
```

To run all the test targets in the project (note that this would be extremely slow and is not recommended):

```
bazel test //...
```

To run multiple test targets at once:

```
bazel test -- //path/to/target/FirstTest //path/to/target/SecondTest
```

## Concepts and Terminology
**[Workspace](https://github.com/oppia/oppia-android/blob/7344270032ac242b1b8987f1b51c8b5aa4f14ce3/WORKSPACE#L2)**

A workspace is a directory where we specify the targeted SDK version, required dependencies, and corresponding Rules. The directory containing the WORKSPACE file is the root of the main repository, which in our case, is the `oppia-android` root directory.
**[Packages](https://github.com/oppia/oppia-android/tree/develop/app)**

A package is defined as a directory containing a file named BUILD or BUILD.bazel.

**[Binary rules](https://github.com/oppia/oppia-android/blob/ba8d914480251e4a8543feb63a93b6c91e0a5a2f/BUILD.bazel#L3)**

A rule specifies the relationship between inputs and outputs, and the steps to build the outputs.
In Android, rules are defined using `android_binary`. Android rules for testing are `android_instrumentation_test` and `android_local_test`.

**[BUILD files](https://github.com/oppia/oppia-android/blob/7344270032ac242b1b8987f1b51c8b5aa4f14ce3/app/BUILD.bazel#L3)**

Every package contains a BUILD file. This file is written in Starlark Language. In this Build file for module-level, we generally define `android_library`, `kt_android_library` to build our package files as per the requirement.

**[Dependencies](https://github.com/oppia/oppia-android/blob/ba8d914480251e4a8543feb63a93b6c91e0a5a2f/BUILD.bazel#L16)**

A target A depends upon a target B, if B is needed by A at build. `A -> B`

```
deps = [ "//app",]
```
Here, `deps` is used to define the dependencies which is a type of dependencies called `deps dependencies` and it includes the files/directory/target which are dependent. From the above example, the dependency is the `app` target which is defined in the [Build file of app package](https://github.com/oppia/oppia-android/blob/ba8d914480251e4a8543feb63a93b6c91e0a5a2f/app/BUILD.bazel#L616).

Example of Dependencies
1. [srcs dependencies](https://github.com/oppia/oppia-android/blob/ba8d914480251e4a8543feb63a93b6c91e0a5a2f/app/BUILD.bazel#L617)
2. [deps dependencies](https://github.com/oppia/oppia-android/blob/ba8d914480251e4a8543feb63a93b6c91e0a5a2f/app/BUILD.bazel#L622)

**[Loading an extension](https://github.com/oppia/oppia-android/blob/ba8d914480251e4a8543feb63a93b6c91e0a5a2f/app/BUILD.bazel#L13)**

Bazel extensions are files ending in .bzl. Use the load statement to import a symbol from an extension.

```
load("@io_bazel_rules_kotlin//kotlin:android.bzl", "kt_android_library")
```
Here, we are loading `android.bzl` and we are going to use it with a symbol name `kt_android_library`.
Arguments to the load function must be string literals. Load statements must appear at top-level in the file.

**[Visibility of a file target](https://github.com/oppia/oppia-android/blob/ba8d914480251e4a8543feb63a93b6c91e0a5a2f/app/BUILD.bazel#L621)**

With the example from our codebase, target `app` whose visibility is public. 

 - `visibility = ["//visibility:public"],` - Anyone can use this target.
 - `"//visibility:private"` - Only targets in this package can use this target.

**[Testing](https://github.com/oppia/oppia-android/blob/7344270032ac242b1b8987f1b51c8b5aa4f14ce3/app/src/sharedTest/java/org/oppia/android/app/player/exploration/BUILD.bazel#L7)**

When we want to run test cases on Bazel build environment, a test target needs to be set up correctly:

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
    test_manifest = "//app:test_manifest",
    test_class = "org.oppia.android.app.utility.math.MathExpressionAccessibilityUtilTest",
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

Your working set is any files your VCS says are dirty, roughly corresponding to something like git status. By default, the plugin tries to expand the sync to cover any target in your working set. This ensures these files are refreshed without having to go to the trouble of adding a temporary target to your project view.
- This sometimes causes problems, and you may see a warning in your Bazel sync tool window to disable it.

**Non-Incrementally Sync Project with BUILD Files**

This option recomputes certain things that are otherwise cached. 
- You should never have to use this option, but it exists for debugging/fallback purposes.

**Sync Working Set**

Your working set is any files your VCS says are dirty, roughly corresponding to something like git status. This option tries to sync only your working set, cutting down on sync time, which is useful if you want to quickly bring in new dependencies in some files you are working on.

**Partially Sync File with Bazel**

Syncs only the targets corresponding to the file from which this action is invoked. Can cut down on the time to sync the project if you’re only interested in resolving a single file.
- This is invoked from the current active file.
- It can also be accessed via right-clicking on a file and selecting the option.
- This is useful for resolving a file marked as “(Unsynced)” or ensuring API changes are recognized by other targets.
- This can be used on directories which is especially useful for resyncing multiple libraries or regenerating protos.

**Automatic Sync**

You can enable automatic syncing in **Settings > Other Settings > Bazel > Auto sync on**. Automatic syncing occurs whenever BUILD files change.
- We do not recommend enabling this option for now, as it can be slow and disruptive. Because of performance concerns, it is disabled by default.