## Table of Contents

- [Dagger Unresolved reference](#dagger-unresolved-reference)
- [Crashing layout tags in tablet](#crashing-layout-tags-in-tablet)
- [Push failed](#push-failed)
- [Facing error while debugging code](#facing-error-while-debugging-code)
  - [Benefits](#benefits)
  - [How to Write a Debugging Doc](#how-to-write-a-debugging-doc)

## Dagger Unresolved reference
**Error**: Unresolved reference `DaggerXXTest_TestApplicationComponent`

**Solution**: Don't worry this is not an error. Just run the test file and it will solve the error. For running tests, you can see [Oppia Android Testing](https://github.com/oppia/oppia-android/wiki/Oppia-Android-Testing) document.

## Crashing layout tags in tablet
**Error**: java.lang.IllegalArgumentException: The tag for topic_lessons_title is invalid. Received: layout-sw600dp-port/topic_lessons_title_0
**Solutions**: This error occurs when we remove any xml file which is related to tablet devices
To solve this
1. Uninstall the app from tablet
2. Rebuild the app.
3. Run the app again.

## Push failed
**Error**: Failed to push some refs to `git@github.com:<your_user_name>/oppia-android.git`

**Solution**: If you are using Android Studio or another UI-based git to push and got the above error, this might not provide many details. Try to push using the command line. Also, we suggest using the command line, as all our scripts, project building, APK building are done using the command line as we are moving towards the Bazel build system and removing the Gradle build system. 

<img src="https://i.imgur.com/0iDpCSO.png" width=400 height=250 />

If there is an error with any of the lint failures it will be detailed in the command line itself. 

## Facing error while debugging code
If you are running into an error that you cannot debug, follow the guidelines outlined on this [wiki page](https://github.com/oppia/oppia-android/wiki/Debugging).
