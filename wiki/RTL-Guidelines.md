# What is RTL?
The main difference between left-to-right (LTR) and right-to-left (RTL) language scripts is the direction in which content is displayed:

* LTR languages display content from left to right
* RTL languages display content from right to left

RTL content also affects the direction in which some icons and images are displayed, particularly those depicting a sequence of events.

In general, the passage of time is depicted as left to right for LTR languages, and right to left for RTL languages.

![](https://user-images.githubusercontent.com/53938155/145036934-691c6bda-a58b-4977-9247-cb6e3830dee7.png)

When a UI is changed from LTR to RTL (or vice-versa), it’s often called mirroring. An RTL layout is the mirror image of an LTR layout, and it affects layout, text, and graphics.

# What changes in RTL?

When a UI is mirrored, these changes occur:

* Text fields icons are displayed on the opposite side of a field
* Navigation buttons are displayed in reverse order
* Icons that communicate direction, like arrows, are mirrored
* Text (if it is translated to an RTL language) is aligned to the right

These items are not mirrored:

* Untranslated text (even if it’s part of a phrase)
* Icons that don’t communicate direction, such as a camera
* Numbers, such as those on a clock and phone numbers
* Charts and graphs

Text should always be in the correct direction for the language it’s in. For example, any LTR words, such as a URL, will continue to be shown in an LTR format, even if the rest of the UI is in RTL.

![](https://user-images.githubusercontent.com/53938155/145037261-2f7afb57-cbeb-4a6e-8ac7-c47261790945.png)

# Testing app for RTL Layouts
To test any screen for RTL language follow these steps:
1. Enable [`Developer Options`](https://developer.android.com/studio/debug/dev-options) on your device.
2. Set language to `Arabic [XB]` or `cibarA [XB]` with the help of this [link](https://developer.android.com/guide/topics/resources/pseudolocales).

Make this language as primary language from your `Language Preferences` in `Settings` and now open the oppia app. You will notice that all characters appear from right-to-left similar to what `Arabic` languages follow.
The screen will look something like this:

<img src="https://user-images.githubusercontent.com/9396084/103191486-364ded80-48fb-11eb-8eb5-461704c150f5.png" width="200" />

# Reference Documentation
* [Oppia-Android RTL Issues](https://docs.google.com/document/d/1Fl1ar5vcdLvay7ZIJLUFQro1wEf1yUEicwF-CKcvwJ0/edit#)
* [RTL Support Milestone](https://github.com/oppia/oppia-android/milestone/40) 
* [Guidelines for RTL](https://material.io/design/usability/bidirectionality.html)
