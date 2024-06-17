Here are some general troubleshooting tips for oppia-android. The specific platforms are Linux, Windows and Mac OS.

## Table of Contents

- [General issues](#general-issues)
- [Bazel issues](#bazel-issues)
- [Can’t find a particular issue?](#cant-find-a-particular-issue)

### General issues

1. If you find any error related to `cURL`, please set up cURL on your machine. For Linux, you can use `sudo apt install curl`. No need to set up `cURL` for Windows as you are using git bash command line.<br>


2. If you find any error which says `java: command not found`, please check you have Java installed correctly in your machine and the [environment path variable](https://www.java.com/en/download/help/path.html) is also set up correctly.


3. If you find any error related to Kotlin or Java/Checkstyle while pushing the code, please check [this link](https://github.com/oppia/oppia-android/wiki/Frequent-Errors-and-Solutions#push-failed).


4. If you see the error

   ```
   Could not resolve protoc-3.8.0-osx-aarch_64.exe (`com.google.protobuf:protoc:3.8.0`)
   ```

   then please follow the 2nd step mentioned in [this wiki](https://github.com/oppia/oppia-android/wiki/Installing-Oppia-Android#install-oppia-android) for Mac with Apple silicon(M1/M2) chips.


5. If you see the error

   ```
   Deprecated Gradle features were used in this build, making it incompatible with Gradle 7.0.
   ```

   then it's fine to ignore it. The message just appears to be a warning. We don't use Gradle 7.0, so this warning is fine to ignore.

6. If you see the error

   ```
   Error `Class 'org.oppia.android.app.profile.PinPasswordActivityTest' not found in module 'oppia-android.app'`
   ```

   or `Module not specified` while running Unit Tests, try to downgrade Android Studio to [Bumblebee (Patch 3)](https://developer.android.com/studio/archive). That should resolve this issue.


7. If you encounter this error while building gradle:

   ```
   > Task :utility:kaptGenerateStubsDebugKotlin FAILED
   Execution failed for task ':utility:kaptGenerateStubsDebugKotlin'.
   > Could not resolve all files for configuration ':utility:debugCompileClasspath'.
      > Failed to transform model.jar (project :model) to match attributes {artifactType=android-classes, org.gradle.category=library, org.gradle.dependency.bundling=external, org.gradle.jvm.version=15, org.gradle.libraryelements=jar, org.gradle.usage=java-api}.
         > Execution failed for JetifyTransform: E:\Android\open-source\oppia-android\model\build\libs\model.jar.
            > Failed to transform 'E:\Android\open-source\oppia-android\model\build\libs\model.jar' using Jetifier. Reason: Unsupported class file major version 59. (Run with --stacktrace for more details.)
   ```
   You are seeing this because Oppia android currently compiles with Java 8, or 9. Higher versions of Java are not supported by our version of Gradle.

   The `model.jar` was compiled with Java 15/major version 59, hence the incompatibility.


   To fix this error, you need to lower the version of Java to compile the JAR file. Please see [here](https://developer.android.com/studio/intro/studio-config#jdk) for more information about Java versions.

### Bazel issues

1. No matching toolchains (sdk_toolchain_type)
    ```
    ERROR: While resolving toolchains for target //:oppia: no matching toolchains found for types
    @bazel_tools//tools/android:sdk_toolchain_type
    ERROR: Analysis of target '//:oppia' failed; build aborted: no matching toolchains found for types
    @bazel_tools//tools/android:sdk_toolchain_type
    INFO: Elapsed time: 12.805s
    INFO: 0 processes.
    FAILED: Build did NOT complete successfully (13 packages loaded, 51 targets configured)
    ```
    [Steps](https://docs.bazel.build/versions/main/tutorial/android-app.html#integrate-with-the-android-sdk) to add ANDROID_HOME environment variable.


2. If you encounter the following:
   ```
      external/bazel_tools/src/tools/android/java/com/google/devtools/build/android/dexer/DexFileSplitter.java:21: error: package com.android.dex does not exist
   import com.android.dex.DexFormat;
                         ^
   ```

   This means that you're still configured for using the custom Oppia Android tool repository for Bazel 4.x (which is no longer needed with Bazel 6.x+). You can fix this by updating your ~/.bazelrc file and either commenting out (e.g. by adding a ``#`` at the start of the line), or removing, the following line:

   ```
   build --override_repository=android_tools=/home/user/opensource/oppia-bazel-tools
   ```

3. **java.lang.ClassNotFoundException: com.android.tools.r8.compatdx.CompatDx**

   If, when building the app binary, you encounter a failure that indicates that the CompatDx file cannot be found, this is likely due to you using a newer version of the Android build tools. You can manually downgrade to an older version of build-tools (particularly 32.0.0). Unfortunately, this can't be done through Android Studio but it can be done over a terminal. Follow the instructions listed [here](https://github.com/oppia/oppia-android/issues/3024#issuecomment-884513455) to downgrade your build tools & then try to build the app again.


4. If you encounter this error while building bazel in Mac M1:
      ```
      ERROR: /Users/OpenSource/oppia-android/model/src/main/proto/BUILD.bazel:167:20: Generating JavaLite proto_library //model/src/main/proto:profile_proto failed: (Segmentation fault): protoc failed: error executing command bazel-out/darwin-opt-exec-2B5CBBC6/bin/external/com_google_protobuf/protoc '--proto_path=bazel-out/android-armeabi-v7a-fastbuild/bin/model/src/main/proto/_virtual_imports/languages_proto' ... (remaining 8 argument(s) skipped)

      Use --sandbox_debug to see verbose messages from the sandbox protoc failed: error executing command bazel-out/darwin-opt-exec-2B5CBBC6/bin/external/com_google_protobuf/protoc '--proto_path=bazel-out/android-armeabi-v7a-fastbuild/bin/model/src/main/proto/_virtual_imports/languages_proto' ... (remaining 8 argument(s) skipped)
      ```
Bazel requires Xcode commandline tools to build on M1, and the Xcode license also needs to be accepted.

   **Follow these steps to solve this error:**

- Install the commandline tools: `xcode-select --install`

- Accept the Xcode licence: `sudo xcodebuild -licence`

- Reset the xcode select path: `sudo xcode-select -r `

 - Set the xcode select path to use CommandLineTools: `sudo xcode-select -s /Library/Developer/CommandLineTools`

- Confirm that the path was correctly set. The expected output is: `/Library/Developer/CommandLineTools`

      xcode-select -p

After successfully running the above commands, build the app using Bazel by running the following command:

      ```
      bazel clean --expunge
      bazel build //:oppia --noexperimental_check_desugar_deps
      ```
The `--noexperimental_check_desugar_deps` flag is explained in the [bazel blog](https://blog.bazel.build/2018/12/19/bazel-0.21.html#android).

### Can’t find a particular issue?

If the error you get is not in the Troubleshooting section above, please post a request for help on the team's discussions board for installation problems: https://github.com/oppia/oppia-android/discussions/categories/q-a-installation.
