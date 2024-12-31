_These instructions are for developers who'd like to contribute code to improve the Oppia platform. If you'd prefer to help out with other things, please see our [general contribution guidelines](https://github.com/oppia/oppia-android/wiki)._

Thanks for your interest in contributing to the Oppia Android project, and making it easier for students to learn online in an effective and enjoyable way!

If you run into any problems along the way, we're here to help! Check out our [wiki page on getting help](https://github.com/oppia/oppia-android/wiki/Get-Help) for the communication channels you can use. If you find any bugs, you can also file an issue on our [issue tracker](https://github.com/oppia/oppia-android/issues). There are also lots of helpful resources in the sidebar, check that out too!

**Important! Please read this page in its entirety before making any code changes.** It contains lots of really important information. You should also read through our [guide to making pull requests](https://github.com/oppia/oppia-android/wiki/Guidance-on-submitting-a-PR).

## Table of Contents

- [How to find information on this wiki](#how-to-find-information-on-this-wiki)
- [Onboarding instructions](#onboarding-instructions)
  - [Guidance on submitting a PR](https://github.com/oppia/oppia-android/wiki/Guidance-on-submitting-a-PR)
  - [Developing your skills](https://github.com/oppia/oppia-android/wiki/Developing-skills)
- [Important: Ongoing Bazel migration](#important-ongoing-bazel-migration)
- [Installing the Oppia web app](#installing-the-oppia-web-app)
- [Communication channels](#communication-channels)

## How to find information on this wiki

Please see the animated image below for guidance:

<img width="1074" alt="Screenshot wiki preview" src="https://user-images.githubusercontent.com/76530270/235522765-3d17617e-1f3b-4531-b84c-33af94885863.gif">

## Onboarding instructions

If you'd like to help out with the Android project, please follow the following steps to get started:

### 1. Complete the preliminaries

- Sign the CLA, so that we can accept your contributions. If you're contributing as an individual, use the [individual CLA](https://goo.gl/forms/AttNH80OV0). If your company owns the copyright to your contributions, a company representative should sign the [corporate CLA](https://goo.gl/forms/xDq9gK3Zcv).

- Fill in the [Oppia contributor survey](https://goo.gl/forms/otv30JV3Ihv0dT3C3) to let us know your interests. (You can always change your responses later.) **Make sure to indicate prominently that you are interested in helping out with Android.**

- Say hi and introduce yourself on [GitHub Discussions](https://github.com/oppia/oppia-android/discussions/4788)!

### 2. Set up your development environment

- Follow the instructions on [Installing Oppia Android](https://github.com/oppia/oppia-android/wiki/Installing-Oppia-Android) to prepare your developer environment and install Oppia Android. **Note:** Make sure you have good Internet connectivity when developing on Oppia Android, since this project uses third party libraries which are needed to build the app.

- Familiarize yourself with the resources linked to from the wiki sidebar, especially the [overview of the codebase](https://github.com/oppia/oppia-android/wiki/Overview-of-the-Oppia-Android-codebase-and-architecture), the [coding style guide](https://github.com/oppia/oppia-android/wiki/Coding-style-guide), and the [Frequent Errors and Solutions](https://github.com/oppia/oppia-android/wiki/Frequent-Errors-and-Solutions). (You don't have to read everything else right now, but it's a good idea to be aware of what's available, so that you can refer to it later if needed.)


### 3. Pick your first starter issue!

We suggest choosing your first issue from the [list of good first issues](https://github.com/oppia/oppia-android/labels/good%20first%20issue). These issues are hand-picked to ensure that you don't run into unexpected roadblocks while working on them, and each of them should have clear instructions for new contributors. (If not, please let us know via [GitHub Discussions](https://github.com/oppia/oppia-android/discussions) and we'll try to fix it.)

When you've found an issue you'd like to tackle:

- Leave a comment that describes in detail how you'll tackle it (e.g. explain which file(s) you would modify and what changes you would make), and @-mention the team lead (typically **@BenHenning**). If your explanation makes sense, we'll assign the issue to you.
- Submit a PR, following the [guidance on submitting a PR](https://github.com/oppia/oppia-android/wiki/Guidance-on-submitting-a-PR). (Consider bookmarking this guide in your browser for easy reference. We also suggest bookmarking the ["my issues" page](https://github.com/issues/assigned) so that you can keep track of the issues assigned to you.)
- If you run into any problems, feel free to create a [GitHub Discussion](https://github.com/oppia/oppia-android/discussions) and get help from the Oppia community.

You are also welcome to tackle non-starter issues, but for those, you might need to be more independent, because we might not know how to solve them either!

Once you've merged PRs that correspond to **two** different pre-existing GitHub issues, please fill in [this form](https://forms.gle/NxPjimCMqsSTNUgu5) to be considered for a place on a team at Oppia Android! A team lead will evaluate your contributions and give you access to the repository so that you can assign yourself to issues. (But please don't create your own issues and then make PRs for them -- that won't count.)



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

If you run into any problems (including non-coding ones), you can ask questions on [GitHub Discussions](https://github.com/oppia/oppia-android/discussions). You can use this avenue for asking anything -- questions about any issue, who to contact for specific things, etc.
