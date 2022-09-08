_These instructions are for developers who'd like to contribute code to improve the Oppia platform. If you'd prefer to help out with other things, please see our [[general contribution guidelines|Home]]._

Thanks for your interest in contributing to the Oppia Android project, and making it easier for students to learn online in an effective and enjoyable way!
## Table of Contents

* [Onboarding instructions](#onboarding-instructions)
* [Install oppia-android](#install-oppia-android)
* [Run the app from Android Studio](#run-the-app-from-android-studio)
* [Finding something to do](#finding-something-to-do)
* [Developing your skills](https://github.com/oppia/oppia-android/wiki/Developing-skills)
* [Important: Ongoing Bazel migration](#important-ongoing-bazel-migration)
* [Installing the Oppia web app](#installing-the-oppia-web-app)
* [Communication channels](#communication-channels)

## Onboarding instructions
If you'd like to help out with the Android project, please follow the following steps to get started:

1. Sign the CLA, so that we can accept your contributions. If you're contributing as an individual, use the [individual CLA](https://goo.gl/forms/AttNH80OV0). If your company owns the copyright to your contributions, a company representative should sign the [corporate CLA](https://goo.gl/forms/xDq9gK3Zcv).
2. Fill in the [Oppia contributor survey](https://goo.gl/forms/otv30JV3Ihv0dT3C3) to let us know what your interests are. (You can always change your responses later.) **Make sure to indicate prominently that you are interested in helping out with Android.**
3. Download/Install the latest version of [Android Studio](https://developer.android.com/studio/?gclid=EAIaIQobChMI8fX3n5Lb6AIVmH8rCh24JQsxEAAYASAAEgL4L_D_BwE&gclsrc=aw.ds#downloads). 
4. Download and Install **Java 8** using the links from [the Java website](https://www.java.com/en/download/).
   - **Note for Windows users:** Make sure to also set up the PATH system variable correctly for `Java`, following [these instructions](https://www.java.com/en/download/help/path.html).
   - [Instructions](https://www.java.com/en/download/help/linux_install.html) for Linux users.
   - [Instructions](https://www.java.com/en/download/help/mac_install.html) for Mac users.
5. In Android Studio, open Tools > SDK Manager.
   - In the "SDK Platforms" tab (which is the default), select `API Level 28`.
   - Also, navigate to the "SDK Tools" tab, click the "Show Package Details" checkbox at the bottom right, then click on "Android SDK Build-Tools 31" and select 29.0.2 (this is needed for Bazel support).

   Then, click "Apply" to download and install these two SDKs/Tools.

6. Read the [guidance on submitting a PR](https://github.com/oppia/oppia-android/wiki/Guidance-on-submitting-a-PR) carefully. You'll need to understand this process well in order to submit PRs to the project!


**Note:** Make sure you have good Internet connectivity when developing on Oppia Android, since this project uses third party libraries which will be needed to build the app.

## Install oppia-android

Please follow these steps to set up Oppia Android on your local machine.

1. Create a new, empty folder called `opensource/` within your home folder. Navigate to it (`cd opensource`), then [fork and clone](https://github.com/oppia/oppia-android/wiki/Fork-and-Clone-Oppia-Android) the Oppia-Android repo. This will create a new folder named `opensource/oppia-android`. Note that contributors who have write access to the repository may either create branches directly on oppia/oppia-android or use a fork.

    **Note**: Please keep the folder name as `oppia-android`. Changing the project folder name might lead to future issues with running the pre-push checks on your machine.

2. Run the setup script, which adds some development tools for Oppia Android (ktlint, checkstyle, etc.):

    - For Mac or Linux
       1. Open a terminal and navigate to `opensource/oppia-android/`.
       2. Run the script `bash scripts/setup.sh`.

    - For Mac with Apple M1 chip
       1. Locate Terminal in Finder.
       2. Right-click and create a duplicate Terminal (and rename it accordingly, say Terminal x86, to avoid confusion).
       3. In the Terminal x86, right-click and click "Get info", and check the option "Open using Rosetta".
       4. Navigate to `opensource/oppia-android/` in Rosetta.
       5. Finally, run `bash scripts/setup.sh` in Terminal x86 and all the required files should be generated. (You should see messages like `Ktlint file downloaded`, etc.)

    - For Windows
       1. Install [Git Bash Command Line](https://gitforwindows.org/)
       2. Open Git Bash Command Line.
       3. Navigate to `opensource/oppia-android/`.
       4. Run the script `bash scripts/setup.sh`.
       5. Download the [google_checks.xml](https://github.com/checkstyle/checkstyle/blob/14005e371803bd52dff429904b354dc3e72638c0/src/main/resources/google_checks.xml) file. To do this, you can simply right-click on the `Raw` button and click on `Save Link as`.
       6. Copy this file to the directory where Git is installed (usually C:/Program Files/Git/).

3. In Android Studio, select `File > Open`, navigate to `opensource/oppia-android/`, and click `OK` to load the project.

4. Click the elephant icon in the toolbar ("Sync Gradle") to ensure that all the correct dependencies are downloaded. (In general, you'll want to do this step any time you update your dependencies.)

## Run the app from Android Studio

1. Go to Tools > AVD Manager, click "Create Virtual Device...". Then:
   - Select a preferred device definition. In general, any device is fine, but you can use Pixel 3a as a default (if you're developing for phones) or Nexus 7 (if you're developing for tablets). After selecting a device, click "Next" at the bottom right to continue.
   - Select a system image (in general, API Level 28, unless you're an M1 Mac user, in which case use API Level 29). Then click "Next".
   - Click "Finish" to complete setup.

2. To run the app, select the emulator device you want from the dropdown menu to the left of the "Run" button in the toolbar.

3. Finally, click the "Run" button.


## Finding something to do
**Starter projects for new contributors**

Welcome! Please make sure to follow the onboarding instructions above if you haven’t already.

Also, read the [guidance on submitting a PR](https://github.com/oppia/oppia-android/wiki/Guidance-on-submitting-a-PR). 

After that, we’d strongly recommend tackling some part of one of the following starter issues.

**Testing**

 - [Add missing tests](https://github.com/oppia/oppia-android/issues?q=is%3Aopen+is%3Aissue+label%3A%22good+first+issue%22+tests)

**UI**

 - [Merge different versions of a xml into a single xml file](https://github.com/oppia/oppia-android/issues?q=is%3Aopen+is%3Aissue+label%3A%22good+first+issue%22+single+xml+file)
 - [Supporting dark mode](https://github.com/oppia/oppia-android/issues?q=is%3Aissue+in%3Atitle+add+dark+mode+is%3Aopen) (see also [these instructions](https://github.com/oppia/oppia-android/wiki/Dark-Mode)).

**Bazel**

**Accessibility**

 - [Accessibility support](https://github.com/oppia/oppia-android/issues?q=is%3Aissue+is%3Aopen+a11y+no%3Aassignee)


## Important: Ongoing Bazel migration

The team is currently using two build systems for the project: Gradle and Bazel. We're in the process of actively migrating to Bazel.

Please note that:
* It's currently recommended that all team members use **Gradle** for their active development in Android Studio. While some team members use the Bazel Android Studio plugin instead of Android Gradle Plugin (AGP), we make this recommendation because day-to-day Bazel development currently suffers from:
  * Significant memory overhead that continues to grow without careful pruning (i.e. periodic shutdowns of the local Bazel build server). On some Linux distros, this can result in a Kernel panic when memory is fully exhausted.
  * Various symbolic errors throughout the codebase that can make it much more difficult to jump to specific symbols (though, unlike Gradle, all code including scripts are editable and runnable within Android Studio).
* That said, when submitting a PR for review, you may notice that some Bazel-specific tests or workflows fail. Investigating and fixing these will require setting up Bazel in your local environment (see the instructions [here](https://github.com/oppia/oppia-android/wiki/Oppia-Bazel-Setup-Instructions)), and then running the specific Bazel commands in your local repository (most team members just use the console within Android Studio to run their Bazel commands).
* Bazel & Gradle sometimes don't play nicely with one another. So, when you're verifying Bazel-specific things, we recommend doing so in one go, and then deleting the corresponding Bazel build artifacts using ``bazel clean`` before switching back over to Gradle (to avoid any issues with the two build systems crossing). Note that Bazel generally doesn't have any problems with Gradle build artifacts, so there's no need to clean the Gradle project first.
* As the team finishes the migration to Bazel, communications and instructions will be sent ahead of time for moving development environments away from Gradle so that we can officially deprecate it.

## Installing the Oppia web app

If you need to connect to a local version of the Oppia web application, check out a copy of the [Oppia web app repository](https://github.com/oppia/oppia) and get it running locally. This will allow you to connect to a local instance of the web app when developing locally.

For now, you generally won't need to do this, until the Android app supports on-the-fly lesson downloading (which we're currently working on).


### Notes

* Our central development branch is `develop`, which should be clean and ready for release at any time. All changes should be done in feature branches based off of `develop`.

* To find the author of a particular change in a file, run this command:

  ```
  git blame file-name
  ```
  The output will show the latest commit SHA, author, date, and time of commit for each line.

  To confine the search of an author between particular lines in a file, you can use:

  ```
  git blame -L 40,60 file-name
  ```
  The output will then show lines 40 to 60 of the particular file.

  For more `git blame` options, you can visit the [git blame documentation](https://git-scm.com/docs/git-blame).

* If your PR includes changing the location of the file, if you simply move the file by cut and paste method, then the git will track it as a new file. So to prevent this, use:
  ```
  git mv old_file_path new_file_path
  ```
  By using this command git will detect the file as a renamed file.


## Communication channels

### Mailing lists

We have a developer chat room [here](https://gitter.im/oppia/oppia-android). Feel free to drop in and say hi!

If you run into any problems, you can ask questions on [GitHub Discussions](https://github.com/oppia/oppia-android/discussions). You can also check out the [developer mailing list](https://groups.google.com/forum/?fromgroups#!forum/oppia-android-dev).