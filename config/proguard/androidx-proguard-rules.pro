# Proguard rules to workaround issues with AndroidX referencing newer APIs.

# AndroidX Room uses reflection. Reference: https://stackoverflow.com/a/58529027.
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# CoordinatorLayout uses reflection to load Behavior subclasses (see #4712 for context), so they
# can't be renamed.
-keep class * extends androidx.coordinatorlayout.widget.CoordinatorLayout$Behavior
-keepclassmembers class * extends androidx.coordinatorlayout.widget.CoordinatorLayout$Behavior {
    # Constructors may be referenced via reflection, so make sure they don't get removed.
    <init>(...);
}

# Ensure that lifecycle-related components (such as LiveData) work correctly. For context, see:
# https://github.com/oppia/oppia-android/issues/3810#issuecomment-931925578.
-keepclassmembers enum androidx.lifecycle.Lifecycle$Event {
    <fields>;
}
-keep !interface * implements androidx.lifecycle.LifecycleObserver {}
-keep class * implements androidx.lifecycle.GeneratedAdapter {
    <init>(...);
}
-keepclassmembers class ** {
    @androidx.lifecycle.OnLifecycleEvent *;
}
-keepclassmembers class androidx.lifecycle.ReportFragment$LifecycleCallbacks { *; }
