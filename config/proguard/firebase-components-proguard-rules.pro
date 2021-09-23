# Reference: https://github.com/firebase/firebase-android-sdk/blob/82b02af331/firebase-components/proguard.txt.

-dontwarn com.google.firebase.components.Component$Instantiation
-dontwarn com.google.firebase.components.Component$ComponentType

-keep class * implements com.google.firebase.components.ComponentRegistrar
