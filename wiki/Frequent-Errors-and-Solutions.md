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
Whenever you are debugging a problem, you may find it useful to keep a record of your debugging process. We often do this already in issues. Issues usually begin with a detailed description of the problem, which is followed by discussion, reports of attempted debugging steps, and what root cause was identified. However, issues' linear comment structure makes them more amenable to communication among team members than organizing your thoughts. Debugging docs, on the other hand, serve primarily to organize your thoughts.

### Benefits
Primarily, debugging docs help keep your thoughts organized. When you have written down the steps you've already tried and the results of your investigations, you don't have to worry about forgetting your earlier work. Further, when you document your work, you force yourself to reflect on what you've already tried. Debugging docs also make it easy for you to bring someone else up to speed on your bug. 

### How to Write a Debugging Doc

#### 1. Begin with Describing What Goes Wrong
Your description should include:
 - Context: What branch or PR is the bug happening on? If this is happening locally, what system are you running, and what code changes have you made?
 - How the Bug Manifests: This might include error logs that you pasted into the document, screenshots, or links to a test run on a PR. If you provide a link, copy the relevant part of the page you are linking to. This keeps the document self-contained so we can search them. It also makes the doc easier for people to read.

#### 2. Describe Your Investigations
What did you try, and what happened when you tried it? You want to include enough detail so that someone could reproduce your investigation to see if they get the same results.

#### 3. Document Your Guesses and Testing
After some investigation, you might have some ideas for what might be going wrong. Document your guesses and describe how you go about testing them. Report the results of that testing and describe whether you think your guess was right. What's your reasoning?

#### 4. Continue/Review from Mentor
Keep going! Continue documenting your investigations, guesses, and tests of those guesses. You can share your debugging doc with your assigned onboarding mentor to review and help you in finding the root cause of the issue or the solution. 

#### 5. Document Your Solution
Once you figure out what the problem was, write that down too! Include an analysis of how the root cause you identify caused the errors you described at the top. Often, this will take the form of one of your suspected solutions.

#### Get Started
Ready to get started with your own debugging doc? You can make a copy of [this template](https://docs.google.com/document/d/1OBAio60bchrNCpIrPBY2ResjeR11ekcN0w5kNJ0DHw8/edit?usp=sharing) to get started. 