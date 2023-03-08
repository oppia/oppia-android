Here are some general troubleshooting tips for oppia-android. The specific platforms are Linux, Windows and Mac OS.

### General issues

1. If you find any error related to `cURL`, please set up cURL on your machine. For Linux, you can use `sudo apt install curl`. No need to set up `cURL` for Windows as you are using git bash command line.<br>

2. If you find any error which says `java: command not found`, please check you have Java installed correctly in your machine and the [environment path variable](https://www.java.com/en/download/help/path.html) is also set up correctly.

3. If you find any error related to Kotlin or Java/Checkstyle while pushing the code, please check [this link](https://github.com/oppia/oppia-android/wiki/Android-Studio-UI-based-Github-workflow#how-to-fix-push-failures).

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
   ERROR: While parsing option --override_repository=android_tools=~/oppia-bazel/android_tools: Repository 
   override directory must be an absolute path
   ```
   
   Try to delete the `.bazelrc` file to solve the above error. 

3. **java.lang.ClassNotFoundException: com.android.tools.r8.compatdx.CompatDx**

   If, when building the app binary, you encounter a failure that indicates that the CompatDx file cannot be found, this is likely due to you using a newer version of the Android build tools. You can manually downgrade to an older version of build-tools (particularly 29.0.2). Unfortunately, this can't be done through Android Studio but it can be done over a terminal. Follow the instructions listed [here](https://github.com/oppia/oppia-android/issues/3024#issuecomment-884513455) to downgrade your build tools & then try to build the app again.


### Can’t find a particular issue?

If the error you get is not in the Troubleshooting section above, please create an issue providing all the necessary information and assign it to **@MohitGupta121**.
