Here are some general troubleshooting tips for oppia-android. The specific platforms are Linux, Windows and Mac OS.

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

2. If you find any error related to `cURL`, please set up cURL on your machine. For Linux, you can use `sudo apt install curl`. No need to set up `cURL` for Windows as you are using git bash command line.<br>

3. If you find any error which says `java: command not found`, please check you have Java installed correctly in your machine and the [environment path variable](https://www.java.com/en/download/help/path.html) is also set up correctly.

4. If you find any error related to Kotlin or Java/Checkstyle while pushing the code, please check [this link](https://github.com/oppia/oppia-android/wiki/Android-Studio-UI-based-Github-workflow#how-to-fix-push-failures).

**Can’t find the particular issue?**

If the error you get is not in the Troubleshooting section above, please create an issue providing all the necessary information and assign it to **@FareesHussain**.
