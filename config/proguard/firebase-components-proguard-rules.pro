# Reference: https://github.com/firebase/firebase-android-sdk/blob/82b02af331/firebase-components/proguard.txt.
# Reference: https://github.com/firebase/firebase-android-sdk/blob/00d4626/firebase-firestore/proguard.txt.

-dontwarn com.google.firebase.components.Component$Instantiation
-dontwarn com.google.firebase.components.Component$ComponentType
-dontwarn javax.naming.**

-keep class * implements com.google.firebase.components.ComponentRegistrar
