## Unresolved reference Dagger
**Error**: Unresolved reference `DaggerXXTest_TestApplicationComponent`

**Solution**: Don't worry this is not an error. Just run the test file and it will solve the error. For running tests you can see [Oppia Android Testing](https://github.com/oppia/oppia-android/wiki/Oppia-Android-Testing) document.

## Push failed
**Error**: Failed to push some refs to `git@github.com:<your_user_name>/oppia-android.git`

**Solution**: If you are using Android Studio or other UI based git to push and got the above error, this might not provide many details. Try to push using the command line. Also, we suggest using the command line, as all our scripts, project building, APK building is done using the command line as we are moving towards the Bazel build system and removing the Gradle build system. 

<img src="https://i.imgur.com/0iDpCSO.png" width=400 height=250 />

If there is any error with any of the lint failure it will be detailed in the command line itself. 

