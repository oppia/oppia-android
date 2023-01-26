_These instructions are for developers who'd like to contribute code to improve the Oppia platform. If you'd prefer to help out with other things, please see our [[general contribution guidelines|Home]]._

Thanks for your interest in contributing to the Oppia Android project, and making it easier for students to learn online in an effective and enjoyable way!

If you run into any problems along the way, we're here to help! Check out our [[wiki page on getting help|Get-Help]] for the communication channels you can use. If you find any bugs, you can also file an issue on our [issue tracker](https://github.com/oppia/oppia-android/issues). There are also lots of helpful resources in the sidebar, check that out too!

**Important! Please read this page in its entirety before making any code changes.** It contains lots of really important information. You should also read through our [guide to making pull requests](https://github.com/oppia/oppia-android/wiki/Guidance-on-submitting-a-PR).

## Table of Contents

- [Onboarding instructions](#onboarding-instructions)
  - [Guidance on submitting a PR](https://github.com/oppia/oppia-android/wiki/Guidance-on-submitting-a-PR)
- [Install oppia-android](#install-oppia-android)
- [Run the app from Android Studio](#run-the-app-from-android-studio)
- [Finding something to do](#finding-something-to-do)
- [Developing your skills](https://github.com/oppia/oppia-android/wiki/Developing-skills)
- [Important: Ongoing Bazel migration](#important-ongoing-bazel-migration)
- [Installing the Oppia web app](#installing-the-oppia-web-app)
- [Communication channels](#communication-channels)

## Onboarding instructions

If you'd like to help out with the Android project, please follow the following steps to get started:

1. Sign the CLA, so that we can accept your contributions. If you're contributing as an individual, use the [individual CLA](https://goo.gl/forms/AttNH80OV0). If your company owns the copyright to your contributions, a company representative should sign the [corporate CLA](https://goo.gl/forms/xDq9gK3Zcv).

2. Fill in the [Oppia contributor survey](https://goo.gl/forms/otv30JV3Ihv0dT3C3) to let us know what your interests are. (You can always change your responses later.) **Make sure to indicate prominently that you are interested in helping out with Android.**

3. Say hi and introduce yourself on [GitHub Discussions](https://github.com/oppia/oppia-android/discussions/4788)!

4. Download/Install the latest version of [Android Studio](https://developer.android.com/studio/?gclid=EAIaIQobChMI8fX3n5Lb6AIVmH8rCh24JQsxEAAYASAAEgL4L_D_BwE&gclsrc=aw.ds#downloads). 

5. Download and Install **Java 8** using the links from [the Java website](https://www.java.com/en/download/).
   - **Note for Windows users:** Make sure to also set up the PATH system variable correctly for `Java`, following [these instructions](https://www.java.com/en/download/help/path.html).
   - [Instructions](https://www.java.com/en/download/help/linux_install.html) for Linux users.
   - [Instructions](https://www.java.com/en/download/help/mac_install.html) for Mac users.

6. In Android Studio, open Tools > SDK Manager.
   - In the "SDK Platforms" tab (which is the default), select `API Level 28`.
   - Also, navigate to the "SDK Tools" tab, click the "Show Package Details" checkbox at the bottom right, then click on "Android SDK Build-Tools 31" and select 29.0.2 (this is needed for Bazel support).

   Then, click "Apply" to download and install these two SDKs/Tools.

7. Familiarize yourself with the resources linked to from the sidebar of this page, especially the [overview of the codebase](https://github.com/oppia/oppia-android/wiki/Overview-of-the-Oppia-Android-codebase-and-architecture), the [[coding style guide|Coding-style-guide]], and the [[Frequent Errors and Solutions|Frequent-Errors-and-Solutions]]. You don't have to read all the other stuff right now, but it's a good idea to be aware of what's available, so that you can refer to it later if needed.

8. Take up your first Oppia Android starter issue! (See [below](https://github.com/oppia/oppia-android/wiki/Contributing-to-Oppia-android#finding-something-to-do) on how to do this.) Make sure to read and follow the [[PR instructions|Guidance-on-submitting-a-PR]] closely so that your PR review proceeds smoothly.

   - In your browser, consider bookmarking the [[guide to making pull requests|Guidance-on-submitting-a-PR]] for easy reference later, as well as the ["my issues" page](https://github.com/issues/assigned) (so that you can keep track of the issues assigned to you).

   - Facing any problems (including non-coding ones)? Please feel free to create a [GitHub Discussion](https://github.com/oppia/oppia-android/discussions) and get help from the Oppia community. You can use this avenue for asking anything -- questions about any issue, who to contact for specific things, etc.

   - We also have onboarding mentors who would be more than happy to help you take your first steps in open source. If you'd like individual support, feel free to request a mentor [using this form](https://forms.gle/udsRP4WQgLcez9Zm8).
 
9. When you have merged PRs that correspond to two different pre-existing GitHub issues, please fill in [this form](https://forms.gle/NxPjimCMqsSTNUgu5) to become an Oppia collaborator! This will grant you access to the repository, and allow you to join a team. (But please don't create your own issues and then make PRs for them -- that won't count.)
 

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

**Good first issues for new contributors**

Welcome! Please make sure to follow the onboarding instructions above if you haven’t already. Also, read the [guidance on submitting a PR](https://github.com/oppia/oppia-android/wiki/Guidance-on-submitting-a-PR). 

After that, you can choose a good first issue from the [list of good first issues](https://github.com/oppia/oppia-android/labels/good%20first%20issue). These issues are hand-picked to ensure that you don't run into unexpected roadblocks while working on them, and each of them should have clear instructions for new contributors. If you see one that doesn't, please let us know via [GitHub Discussions](https://github.com/oppia/oppia-android/discussions) and we'll try to fix it. For other issues, you might need to be more independent because we might not know how to solve them either.

When you've found an issue you'd like to tackle, please leave a comment on it that:

- @-mentions the team lead (typically **@BenHenning**), letting them know you'd like to work on it.
- describes in detail how you'd tackle the issue (e.g. explain which file(s) you would modify and what changes you would make). 

If your explanation makes sense, we'll assign the issue to you. Feel free to go ahead and submit a PR for it by following the instructions for making a PR! If you run into any issues, feel free to create a [GitHub Discussion](https://github.com/oppia/oppia-android/discussions) and get help from the Oppia community, or [request a mentor](https://forms.gle/udsRP4WQgLcez9Zm8) if you'd like individual support.


## Important: Ongoing Bazel migration

The team is currently using two build systems for the project: Gradle and Bazel. We're in the process of actively migrating to Bazel.

Please note that:
- It's currently recommended that all team members use **Gradle** for their active development in Android Studio. While some team members use the Bazel Android Studio plugin instead of Android Gradle Plugin (AGP), we make this recommendation because day-to-day Bazel development currently suffers from:
  - Significant memory overhead that continues to grow without careful pruning (i.e. periodic shutdowns of the local Bazel build server). On some Linux distros, this can result in a Kernel panic when memory is fully exhausted.
  - Various symbolic errors throughout the codebase that can make it much more difficult to jump to specific symbols (though, unlike Gradle, all code including scripts are editable and runnable within Android Studio).
- That said, when submitting a PR for review, you may notice that some Bazel-specific tests or workflows fail. Investigating and fixing these will require setting up Bazel in your local environment (see the instructions [here](https://github.com/oppia/oppia-android/wiki/Oppia-Bazel-Setup-Instructions)), and then running the specific Bazel commands in your local repository (most team members just use the console within Android Studio to run their Bazel commands).
- Bazel & Gradle sometimes don't play nicely with one another. So, when you're verifying Bazel-specific things, we recommend doing so in one go, and then deleting the corresponding Bazel build artifacts using ``bazel clean`` before switching back over to Gradle (to avoid any issues with the two build systems crossing). Note that Bazel generally doesn't have any problems with Gradle build artifacts, so there's no need to clean the Gradle project first.
- As the team finishes the migration to Bazel, communications and instructions will be sent ahead of time for moving development environments away from Gradle so that we can officially deprecate it.

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

If you run into any problems, you can ask questions on [GitHub Discussions](https://github.com/oppia/oppia-android/discussions). You can also check out the [developer mailing list](https://groups.google.com/forum/?fromgroups#!forum/oppia-android-dev).